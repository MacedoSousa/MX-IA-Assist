UPDATE execution_runs
SET status = 'AWAITING_APPROVAL'
WHERE status = 'WAITING_APPROVAL';
