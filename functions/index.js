const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");

admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

/**
 * Callable function (manual trigger):
 * - Reads Firestore collection "filterNotificationTag"
 * - Time window: last N hours (default 24)
 * - Aggregates by tagName, picks the top one
 * - Sends FCM notification to topic "all"
 *
 * Request body example (from Firebase Console -> Functions -> Testing):
 *   { "windowHours": 24, "title": "Trending filter", "limitHours": 24 }
 */
exports.sendTrendingFilterNotification = functions.https.onCall(async (data, context) => {
  try {
    // (Opcional) exige auth si quieres restringir quién puede ejecutarla:
    // if (!context.auth || !context.auth.token.admin) {
    //   throw new functions.https.HttpsError("permission-denied", "Admin auth required.");
    // }

    const windowHours = Number(data?.windowHours ?? 24);
    const now = admin.firestore.Timestamp.now();
    const fromTs = admin.firestore.Timestamp.fromDate(new Date(Date.now() - windowHours * 3600 * 1000));

    // 1) Lee últimos N horas de "filterNotificationTag"
    const snap = await db.collection("filterNotificationTag")
      .where("ts", ">=", fromTs)
      .orderBy("ts", "desc")
      .limit(5000) // tope de seguridad
      .get();

    // 2) Cuenta ocurrencias por tagName
    const counts = new Map(); // tagName -> count
    for (const doc of snap.docs) {
      const tagName = (doc.get("tagName") || "").toString().trim();
      if (!tagName) continue;
      counts.set(tagName, (counts.get(tagName) || 0) + 1);
    }

    // 3) Determina el más usado (fallback si vacío)
    let topTag = null;
    let topCount = -1;
    for (const [name, count] of counts.entries()) {
      if (count > topCount) {
        topTag = name;
        topCount = count;
      }
    }
    if (!topTag) {
      // Fallback comercial si no hay datos recientes
      topTag = "House";
      topCount = 0;
    }

    // 4) Crea mensaje FCM (notification + data) al topic "all"
    const title = data?.title || "Trending filter";
    const body = `Trending filter: ${topTag}. Explore listings we think you'll love.`; // <- cuerpo comercial
    const message = {
      topic: "all",
      notification: {
        title,
        body,
      },
      data: {
        route: "filterResults",
        tags: topTag, // ¡IMPORTANTE! enviamos NOMBRE, no ID
        title,
        body,
      },
      android: {
        priority: "high",
        notification: { channelId: "welhome_default" }, // usa tu canal si corresponde
      },
      apns: {
        headers: { "apns-priority": "10" },
      },
    };

    const resp = await messaging.send(message);

    return {
      ok: true,
      sent: true,
      fcmId: resp,
      topTag,
      topCount,
      windowHours,
      docsScanned: snap.size,
    };
  } catch (err) {
    console.error("sendTrendingFilterNotification error", err);
    throw new functions.https.HttpsError("internal", err.message || "unknown");
  }
});



// === BigQuery client ===
const {BigQuery} = require('@google-cloud/bigquery');
const bq = new BigQuery();

// Configura tu dataset/tabla
const DATASET = 'welhome';
const TABLE = 'saved_events';

// Firestore → BigQuery (streaming)
exports.onSavedEventCreate = functions.firestore
  .document('savedEvents/{docId}')
  .onCreate(async (snap, ctx) => {
    const d = snap.data() || {};
    // Normaliza
    const row = {
      user_id: d.userId || null,
      housing_id: d.housingId || null,
      nationality: (d.nationality || 'Unknown').toString(),
      // guarda una fila por tag para facilitar agregaciones en SQL
      tags: Array.isArray(d.tags) && d.tags.length ? d.tags : ['Unknown'],
      ts: d.ts ? d.ts.toDate() : new Date(),
    };

    // Inserta 1 fila por tag
    const rows = row.tags.map(tag => ({
      user_id: row.user_id,
      housing_id: row.housing_id,
      nationality: row.nationality,
      tag: tag.toString(),
      ts: row.ts,
    }));

    await bq.dataset(DATASET).table(TABLE).insert(rows, {raw: true});
    return true;
  });

  // HTTPS manual: GET/POST /sendSavedTagNotification?nationality=CO[&tag=Pet%20friendly][&title=...][&body=...]
exports.sendSavedTagNotification = functions.https.onRequest(async (req, res) => {
  try {
    // Auth simple (opcional): revisa una API key si quieres.
    // if (req.get('x-api-key') !== functions.config().admin.apikey) return res.status(403).json({ok:false});

    const nat = (req.query.nationality || req.body?.nationality || '').toString().trim();
    if (!nat) return res.status(400).json({ok:false, error: 'nationality is required'});

    // Tag opcional recibido; si no viene, consulta el TOP histórico en BQ
    let tag = (req.query.tag || req.body?.tag || '').toString().trim();

    if (!tag) {
      const [rows] = await bq.query({
        query: `
          SELECT tag, COUNT(*) c
          FROM \`${DATASET}.${TABLE}\`
          WHERE LOWER(nationality) = LOWER(@nat)
          GROUP BY tag
          ORDER BY c DESC
          LIMIT 1
        `,
        params: { nat },
      });
      tag = rows?.[0]?.tag || 'Housing';
    }

    const title = (req.query.title || req.body?.title || `Popular in ${nat}`).toString();
    const body  = (req.query.body  || req.body?.body  || `Most saved tag for ${nat}: ${tag}`).toString();

    // Enviar a tópico por nacionalidad: "nat_co", "nat_us", etc.
    const topic = 'nat_' + nat.toLowerCase().replace(/\s+/g, '_');

    const message = {
      topic,
      notification: { title, body },
      data: {
        route: 'saved',    // abre tu pestaña Saved; maneja en MyMessagingService si quieres deep link
        tag,               // opcional
        nationality: nat,
        title, body
      },
      android: { priority: 'high', notification: { channelId: 'welhome_default' } },
      apns: { headers: { 'apns-priority': '10' } }
    };

    const id = await admin.messaging().send(message);

    return res.json({ ok: true, fcmId: id, nationality: nat, tag });
  } catch (e) {
    console.error('sendSavedTagNotification error', e);
    return res.status(500).json({ ok:false, error: e.message || 'internal' });
  }
});
