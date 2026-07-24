CREATE TABLE business_pricing_rule (
    id BIGSERIAL PRIMARY KEY,
    business_offering_id BIGINT NOT NULL REFERENCES business_offering(id) ON DELETE CASCADE,
    rule_key VARCHAR(80) NOT NULL,
    rule_type VARCHAR(40) NOT NULL,
    billing_unit VARCHAR(40) NOT NULL,
    amount NUMERIC(12, 2),
    currency VARCHAR(3),
    quantity_from NUMERIC(12, 3),
    quantity_to NUMERIC(12, 3),
    modifier NUMERIC(12, 2),
    conditions JSONB NOT NULL DEFAULT '{}'::jsonb,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_business_pricing_rule_sort_order CHECK (sort_order >= 0),
    CONSTRAINT ck_business_pricing_rule_quantity_range CHECK (
        quantity_to IS NULL OR quantity_from IS NULL OR quantity_to >= quantity_from
    )
);

CREATE UNIQUE INDEX ux_business_pricing_rule_key
    ON business_pricing_rule(business_offering_id, rule_key);
CREATE INDEX ix_business_pricing_rule_active_sort
    ON business_pricing_rule(business_offering_id, active, sort_order, id);
