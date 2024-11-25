SELECT e.enumsortorder, e.enumlabel
FROM pg_catalog.pg_enum e
LEFT JOIN pg_catalog.pg_type t on t.OID = e.ENUMTYPID
LEFT JOIN pg_catalog.pg_user u on t.typowner = u.usesysid
WHERE u.usename = ?USER?
AND t.typname = ?NAME?
order by typname, enumsortorder