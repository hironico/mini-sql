SELECT distinct t.typname
FROM pg_catalog.pg_enum e
LEFT JOIN pg_catalog.pg_type t on t.OID = e.ENUMTYPID
LEFT JOIN pg_catalog.pg_roles r on r.OID = t.TYPOWNER
WHERE r.ROLNAME = '?USER?'
order by typname