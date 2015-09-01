SET @nowdate = now()
go
SET @increment = 1
go
select now() into @nowdate
go
select 1 into @one
go
CALL TEST_SIMPLE_SP(@nowdate, @increment, 'Stored Procedure', @helloSP)
go
select @nowdate,@increment
