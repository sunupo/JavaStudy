To find the time consumed by a MySQL delete query in the log file, you can use the following command in the MySQL console:

```sql
SET profiling = 1;
DELETE FROM table_name WHERE condition;
SHOW PROFILES; -- This will show the profiling information for the most recent query, including the time consumed.
```

----
185110940

## delete的消耗时间，在日志，如何查找。
