SELECT n.nspname as "schema",
  p.proname as "name",
  pg_catalog.pg_get_function_result(p.oid) as "Result data type",
  pg_catalog.pg_get_function_arguments(p.oid) as "Argument data types",
 CASE p.prokind
  WHEN 'a' THEN 'agg'
  WHEN 'w' THEN 'window'
  WHEN 'p' THEN 'proc'
  ELSE 'func'
 END as "Type",
 CASE
  WHEN p.provolatile = 'i' THEN 'immutable'
  WHEN p.provolatile = 's' THEN 'stable'
  WHEN p.provolatile = 'v' THEN 'volatile'
 END as "Volatility",
 CASE
  WHEN p.proparallel = 'r' THEN 'restricted'
  WHEN p.proparallel = 's' THEN 'safe'
  WHEN p.proparallel = 'u' THEN 'unsafe'
 END as "Parallel",
 pg_catalog.pg_get_userbyid(p.proowner) as "owner",
 CASE WHEN prosecdef THEN 'definer' ELSE 'invoker' END AS "security",
 pg_catalog.array_to_string(p.proacl, E'\n') AS "Access privileges",
 l.lanname as "Language",
 COALESCE(pg_catalog.pg_get_function_sqlbody(p.oid), p.prosrc) as "text",
 pg_catalog.obj_description(p.oid, 'pg_proc') as "description"
FROM pg_catalog.pg_proc p
     LEFT JOIN pg_catalog.pg_namespace n ON n.oid = p.pronamespace
     LEFT JOIN pg_catalog.pg_language l ON l.oid = p.prolang
WHERE pg_catalog.pg_function_is_visible(p.oid)
      AND n.nspname = ?SCHEMA?
      AND p.proname = ?NAME?
ORDER BY 1, 2, 4;