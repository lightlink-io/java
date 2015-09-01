
select 1 as ONE from dual
--% sql.query();

select 2 as TWO from dual
--% sql.query();

select :(json)p.jsonData as "(json)jsonOut" from dual

--% sql.query();

SELECT TO_CLOB('CLOB_DATA' ) as CLOB_DATA FROM dual