UPDATE customer 
  SET
		customer_bale=(
SELECT
	SUM( account_bale ) 
FROM
	account 
WHERE
  customer_id=account_custid
GROUP BY
	account_custid)


UPDATE customer c, (SELECT SUM(account_bale) r, account_custid FROM account GROUP BY account_custid) t SET c.customer_bale = t.r WHERE c.customer_id=t.account_custid