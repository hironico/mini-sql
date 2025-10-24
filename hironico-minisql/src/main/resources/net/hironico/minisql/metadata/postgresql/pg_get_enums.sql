SELECT distinct t.typname
FROM   pg_enum e
JOIN   pg_type t ON e.enumtypid = t.oid
JOIN   pg_namespace n ON t.typnamespace = n.oid
WHERE  n.nspname = '?SCHEMA?'
ORDER BY t.typname