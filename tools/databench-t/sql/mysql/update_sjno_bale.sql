UPDATE 
  sjno 
 SET 
   sjno_bale = ( 
	 SELECT 
	  SUM( account_bale ) 
	FROM 
	   account 
	WHERE 
	  sjno_id=account_sjnoid
	AND
	  sjno_branchid=account_branchid
	GROUP BY
	  account_branchid,
	  account_sjnoid
	)
	


UPDATE sjno s,

   ( SELECT SUM( account_bale ) r, account_sjnoid, account_branchid 
  
     FROM account GROUP BY account_sjnoid, account_branchid ) t 
	  
 SET s.sjno_bale = t.r 
 
 WHERE
	s.sjno_id = t.account_sjnoid 
 AND 
    s.sjno_branchid = t.account_branchid