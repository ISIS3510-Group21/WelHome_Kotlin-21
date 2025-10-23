const functions = require("firebase-functions");
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
