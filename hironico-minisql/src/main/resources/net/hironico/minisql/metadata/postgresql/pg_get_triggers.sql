-- portable query to get the triggers info from a schema.table name

SELECT trigger_name,
       event_manipulation,
       action_statement,
       action_timing
FROM   information_schema.triggers
WHERE  trigger_schema = ?SCHEMA?
  AND  event_object_table = ?NAME?