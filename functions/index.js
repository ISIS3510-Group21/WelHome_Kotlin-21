import { onSchedule } from "firebase-functions/v2/scheduler";
import admin from "firebase-admin";
import { BigQuery } from "@google-cloud/bigquery";

try {
  if (!admin.apps.length) admin.initializeApp();
} catch (e) {
  console.warn("Admin initialization skipped:", e);
}

const PROJECT_ID = process.env.GCLOUD_PROJECT;
const DATASET = "analytics_505132659";
const BQ_LOCATION = "US";

const bigquery = new BigQuery({ projectId: PROJECT_ID });

// ------------------------
// Versión corta (cada 2 min) usando 30 HORAS
// ------------------------
export const pushTrendingFiltersShort = onSchedule(
  { schedule: "every 2 minutes", timeZone: "America/Bogota" },
  async () => {
    const LOOKBACK_HOURS = 30;
    const query = `
      SELECT
        (SELECT value.string_value FROM UNNEST(event_params) WHERE key = 'tag_id') AS tag_id,
        COUNT(1) AS cnt
      FROM \`${PROJECT_ID}.${DATASET}.events_*\`
      WHERE event_name = 'filter_search_tag'
        AND _TABLE_SUFFIX BETWEEN FORMAT_DATE('%Y%m%d', DATE_SUB(CURRENT_DATE(), INTERVAL 2 DAY))
                              AND FORMAT_DATE('%Y%m%d', CURRENT_DATE())
        AND TIMESTAMP_MICROS(event_timestamp) >= TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL ${LOOKBACK_HOURS} HOUR)
      GROUP BY tag_id
      HAVING tag_id IS NOT NULL
      ORDER BY cnt DESC
      LIMIT 3;
    `;

    const [job] = await bigquery.createQueryJob({ query, location: BQ_LOCATION });
    const [rows] = await job.getQueryResults();
    if (!rows?.length) { console.log("No trending tags in lookback window."); return; }

    const topTags = rows.map(r => r.tag_id).filter(Boolean);
    const tagsCsv = topTags.join(",");
    await admin.messaging().send({
      topic: "trending_filters",
      notification: { title: "🔥 Filtros populares", body: `Ahora mismo: ${topTags.join(" · ")}` },
      data: { tags: tagsCsv, deep_link: `welhome://filterResults?tags=${encodeURIComponent(tagsCsv)}` },
      android: { priority: "high" },
    });
    console.log("[Short-30h] Sent trending_filters:", tagsCsv);
  }
);

// ------------------------
// Versión full (cada 10 min) usando 30 HORAS + intraday
// ------------------------
export const pushTrendingFiltersFull = onSchedule(
  { schedule: "every 10 minutes", timeZone: "America/Bogota" },
  async () => {
    const LOOKBACK_HOURS = 30;
    const query = `
      WITH unioned AS (
        SELECT event_name, event_timestamp, event_params
        FROM \`${PROJECT_ID}.${DATASET}.events_*\`
        WHERE _TABLE_SUFFIX BETWEEN
          FORMAT_DATE('%Y%m%d', DATE_SUB(CURRENT_DATE(), INTERVAL 2 DAY))
          AND FORMAT_DATE('%Y%m%d', CURRENT_DATE())
        UNION ALL
        SELECT event_name, event_timestamp, event_params
        FROM \`${PROJECT_ID}.${DATASET}.events_intraday_*\`
        WHERE _TABLE_SUFFIX = FORMAT_DATE('%Y%m%d', CURRENT_DATE())
      )
      SELECT
        (SELECT value.string_value FROM UNNEST(event_params) WHERE key = 'tag_id' LIMIT 1) AS tag_id,
        COUNT(1) AS uses
      FROM unioned
      WHERE event_name = 'filter_search_tag'
        AND TIMESTAMP_MICROS(event_timestamp) >= TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL ${LOOKBACK_HOURS} HOUR)
      GROUP BY tag_id
      HAVING tag_id IS NOT NULL
      ORDER BY uses DESC
      LIMIT 3;
    `;

    const [job] = await bigquery.createQueryJob({ query, location: BQ_LOCATION });
    const [rows] = await job.getQueryResults();
    if (!rows?.length) { console.log("No trending tags found."); return; }

    const topTags = rows.map(r => r.tag_id).filter(Boolean);
    const tagsCsv = topTags.join(",");
    await admin.messaging().send({
      topic: "trending_filters",
      notification: { title: "🔥 Filtros en tendencia (Full)", body: `Los más usados: ${topTags.join(" · ")}` },
      data: { tags: tagsCsv, deep_link: `welhome://filterResults?tags=${encodeURIComponent(tagsCsv)}` },
      android: { priority: "high" },
    });
    console.log("[Full-30h] Sent trending_filters:", tagsCsv);
  }
);
