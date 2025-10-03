# Cognee LanceDB S3 Sync Notes

Date: 2025-10-02 / 2025-10-03

## Summary
- `fuzzforge ingest` returned `NoDataError` in multi-tenant S3 mode because LanceDB collections were only written to the local volume (`/app/.cognee_system`) and never mirrored to S3.
- Kuzu (`*.pkl`) already synced correctly; only LanceDB collections were missing.
- Added `_sync_local_db_to_s3()` helper in `cognee/infrastructure/databases/vector/lancedb/LanceDBAdapter.py` and call it after `create_data_points` and `delete_data_points` so every write publishes the Lance directory to S3.
- Rebuilt the `cognee` service container and restarted on port `18000` (port 8000 was in use).
- Wiped the old dataset state (Postgres tables, local Lance directory, S3 prefix) and re-ran `fuzzforge ingest --force ./test_ingest.md`.
- Verified the ingest now succeeds, insights/chunks return data, and LanceDB collections appear under `s3://cognee-bucket/system/databases/<tenant>/<dataset>.lance.db/`.

## Useful Commands
```
# rebuild and restart service
docker compose -f docker-compose.cognee.yaml build cognee
COGNEE_SERVICE_PORT=18000 docker compose -f docker-compose.cognee.yaml up -d cognee

# clean old dataset data
docker compose -f docker-compose.cognee.yaml exec postgres psql -U cognee -d cognee_db \
  -c "DELETE FROM dataset_data; DELETE FROM data; DELETE FROM datasets; DELETE FROM dataset_database;"

# remove local Lance directory & S3 prefix (optional reset)
docker compose -f docker-compose.cognee.yaml exec cognee rm -rf /app/.cognee_system/databases/<tenant-id>
docker compose -f docker-compose.cognee.yaml exec minio-mc mc rm -r --force \
  cognee/cognee-bucket/system/databases/<tenant-id>

# re-ingest project (from project root)
fuzzforge ingest --force ./test_ingest.md

# confirm LanceDB collections in S3
docker compose -f docker-compose.cognee.yaml exec minio-mc \
  mc ls -r cognee/cognee-bucket/system/databases/<tenant-id>/<dataset-id>.lance.db/
```

## Verification
- API search (`CHUNKS` and `INSIGHTS`) now returns data.
- `mc ls` shows LanceDB tables in S3.
- No more `DocumentChunk_text collection not found` errors in the service logs.

