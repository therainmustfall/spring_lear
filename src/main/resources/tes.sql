--DROP FUNCTION IF EXISTS dbo.extractIds
--DROP FUNCTION IF EXISTS dbo.findPar
--DROP FUNCTION IF EXISTS dbo.splitString
--DROP TABLE IF EXISTS deptstd
--DROP TABLE IF EXISTS writer_table
;;;

CREATE FUNCTION [dbo].[extractIds] 
(	
	@field varchar(2000)
)
RETURNS @l TABLE(ids varchar(100))
AS
BEGIN
	DECLARE @id as varchar(100)
	WHILE (CHARINDEX(']',@field) > 0)
	BEGIN
		IF (CHARINDEX(']',@field) - CHARINDEX('[',@field) > 3)
		BEGIN
			SET @id = SUBSTRING(@field, CHARINDEX('[',@field) + 1,CHARINDEX(']',@field)-CHARINDEX('[',@field) -1);
			INSERT @l VALUES(@id);
		END
			
		SET @field = STUFF(@field, 1, CHARINDEX(']', @field, 1), '');
	END
	RETURN
END

;;

CREATE FUNCTION [dbo].[findPar](@child VARCHAR(2000), @field VARCHAR(2000)) 
RETURNS VARCHAR(2000)
AS
BEGIN

	declare @p varchar(2000);
	SET @field = REPLACE(@field,@child+';','')
	WHILE (len(@field) - len(Replace(@field,';','')) > 1) 

		SET @field = SUBSTRING(@field,CHARINDEX(';',@field)+1,2000)

	SET @p = REPLACE(@field,';','')

	RETURN @p
END

;;

CREATE FUNCTION [dbo].[splitString](@field VARCHAR(2000), @sp varchar(100)) 
RETURNS @l Table(res varchar(1000))
AS
BEGIN
	DECLARE @res AS VARCHAR(100);
	-- SET @field = @field + @sp;
	WHILE (@field <> '')
	BEGIN
		SET @res = LEFT(@field, CHARINDEX(@sp,@field,1) -1)
		INSERT @l VALUES(@res);
		SET @field = STUFF(@field, 1, CHARINDEX(@sp, @field, 1), '')
	END
	RETURN
END

;;

--机构关系表
SELECT c.organ parent,c.organid parentid, b.organ,b.organid organid, 
(SELECT a.organ+','  from organinfo a where  dbo.findPar(a.organid,a.organidlevel) = b.organid for xml path('')) children
into temp_deptstd
from organinfo b inner join organinfo c on c.organid = dbo.findPar(b.organid,b.organidlevel);


;;


delete from temp_deptstd where children is null;


;;

--作者id，姓名表

select distinct writerid, writer into temp_writer_table  from writerdic;


;;


--评价对象
--create TABLE temp_tlts (
--	year varchar(max),
--	name varchar(max),
--	stat varchar(max),
--	dept varchar(max),
--	gender varchar(max),
--	birth  varchar(max),
--	email  varchar(max)
--);

--;;

--insert into @talents
--values 
--('WOS:000480683300002'),('WOS:000460493700009'),('WOS:000463695400026'),('WOS:000591695500008'),('WOS:000608259700001'),('WOS:000524479100060'),('WOS:000681134100026'),('WOS:000467641800074'),('WOS:000611850000026'),('WOS:000563052800001'),
--;;


