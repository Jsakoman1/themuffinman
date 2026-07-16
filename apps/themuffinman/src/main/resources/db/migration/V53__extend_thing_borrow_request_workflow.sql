ALTER TABLE thing_borrow_request
    ADD COLUMN approved_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE thing_borrow_request
    DROP CONSTRAINT IF EXISTS thing_borrow_request_status_check;

ALTER TABLE thing_borrow_request
    ADD CONSTRAINT thing_borrow_request_status_check
        CHECK (status IN ('PENDING', 'CANCELLED', 'APPROVED', 'DECLINED', 'RETURNED'));
