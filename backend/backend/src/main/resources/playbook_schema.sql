-- Playbook Automation Module Database Schema

-- 1. Incidents Table
CREATE TABLE IF NOT EXISTS incidents (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    severity VARCHAR(50) NOT NULL CHECK (severity IN ('Low', 'Medium', 'High', 'Critical')),
    status VARCHAR(50) NOT NULL CHECK (status IN ('Open', 'Investigating', 'Resolved', 'Closed')),
    source VARCHAR(100) NOT NULL,
    assigned_to_id BIGINT,
    priority VARCHAR(50),
    escalated BOOLEAN DEFAULT FALSE,
    sla_deadline TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_incidents_assigned_to FOREIGN KEY (assigned_to_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 2. Playbooks Table
CREATE TABLE IF NOT EXISTS playbooks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    trigger_type VARCHAR(50) NOT NULL CHECK (trigger_type IN ('ALERT_SEVERITY', 'ALERT_TYPE', 'MANUAL', 'THREAT_DETECTED')),
    trigger_value VARCHAR(100), -- e.g., 'Critical' or 'Brute Force'
    conditions_json TEXT, -- JSON condition rules
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Playbook Steps Table
CREATE TABLE IF NOT EXISTS playbook_steps (
    id BIGSERIAL PRIMARY KEY,
    playbook_id BIGINT NOT NULL,
    step_order INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    action_type VARCHAR(50) NOT NULL CHECK (action_type IN (
        'ISOLATE_HOST', 
        'BLOCK_IP', 
        'DISABLE_USER', 
        'SCAN_VULNERABILITY', 
        'SEND_NOTIFICATION', 
        'CREATE_INCIDENT',
        'VALIDATE_EMAIL',
        'CHECK_SENDER_REPUTATION',
        'SCAN_URLS',
        'SCAN_ATTACHMENTS',
        'CALCULATE_RISK_SCORE',
        'DECISION_CONTAINMENT',
        'VERIFY_HEARTBEAT',
        'IDENTIFY_CRITICALITY',
        'TRIGGER_DIAGNOSTICS'
    )),
    parameters_json TEXT, -- JSON arguments for step actions
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_playbook_steps_playbook FOREIGN KEY (playbook_id) REFERENCES playbooks(id) ON DELETE CASCADE,
    CONSTRAINT uq_playbook_steps_order UNIQUE (playbook_id, step_order)
);

-- 4. Playbook Executions Table
CREATE TABLE IF NOT EXISTS playbook_executions (
    id BIGSERIAL PRIMARY KEY,
    playbook_id BIGINT NOT NULL,
    incident_id BIGINT,
    alert_id BIGINT,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED')),
    current_step_index INT NOT NULL DEFAULT 0,
    progress INT NOT NULL DEFAULT 0 CHECK (progress BETWEEN 0 AND 100),
    triggered_by_id BIGINT,
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_playbook_executions_playbook FOREIGN KEY (playbook_id) REFERENCES playbooks(id) ON DELETE CASCADE,
    CONSTRAINT fk_playbook_executions_incident FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE SET NULL,
    CONSTRAINT fk_playbook_executions_alert FOREIGN KEY (alert_id) REFERENCES alerts(id) ON DELETE SET NULL,
    CONSTRAINT fk_playbook_executions_triggered_by FOREIGN KEY (triggered_by_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 5. Playbook Execution Logs Table
CREATE TABLE IF NOT EXISTS playbook_execution_logs (
    id BIGSERIAL PRIMARY KEY,
    execution_id BIGINT NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED')),
    log_level VARCHAR(20) NOT NULL DEFAULT 'INFO' CHECK (log_level IN ('INFO', 'WARN', 'ERROR')),
    message TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_playbook_execution_logs_execution FOREIGN KEY (execution_id) REFERENCES playbook_executions(id) ON DELETE CASCADE
);

-- 6. Playbook Audit Logs Table
CREATE TABLE IF NOT EXISTS playbook_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action_type VARCHAR(50) NOT NULL CHECK (action_type IN (
        'CREATE_PLAYBOOK', 
        'UPDATE_PLAYBOOK', 
        'DELETE_PLAYBOOK', 
        'TRIGGER_PLAYBOOK', 
        'STEP_EXECUTE'
    )),
    entity_id BIGINT,
    performed_by_id BIGINT,
    details TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_playbook_audit_logs_performed_by FOREIGN KEY (performed_by_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 7. Playbook Notifications Table
CREATE TABLE IF NOT EXISTS playbook_notifications (
    id BIGSERIAL PRIMARY KEY,
    execution_id BIGINT NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    channel VARCHAR(50) NOT NULL CHECK (channel IN ('EMAIL', 'SLACK', 'IN_APP')),
    message TEXT NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    CONSTRAINT fk_playbook_notifications_execution FOREIGN KEY (execution_id) REFERENCES playbook_executions(id) ON DELETE CASCADE
);

-- Optimization Indexes
CREATE INDEX IF NOT EXISTS idx_playbook_steps_playbook_id ON playbook_steps(playbook_id);
CREATE INDEX IF NOT EXISTS idx_playbook_executions_playbook_id ON playbook_executions(playbook_id);
CREATE INDEX IF NOT EXISTS idx_playbook_executions_incident_id ON playbook_executions(incident_id);
CREATE INDEX IF NOT EXISTS idx_playbook_executions_alert_id ON playbook_executions(alert_id);
CREATE INDEX IF NOT EXISTS idx_playbook_execution_logs_execution_id ON playbook_execution_logs(execution_id);

-- 8. Knowledge Base Articles Table
CREATE TABLE IF NOT EXISTS kb_articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('RUNBOOK', 'POST_INCIDENT_REVIEW', 'DETECTION_RULE')),
    version INT NOT NULL DEFAULT 1,
    created_by_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_kb_articles_created_by FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 9. Knowledge Base Article Revisions Table
CREATE TABLE IF NOT EXISTS kb_article_revisions (
    id BIGSERIAL PRIMARY KEY,
    article_id BIGINT NOT NULL,
    version INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    updated_by_id BIGINT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_kb_revisions_article FOREIGN KEY (article_id) REFERENCES kb_articles(id) ON DELETE CASCADE,
    CONSTRAINT fk_kb_revisions_updated_by FOREIGN KEY (updated_by_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 10. Incident - KB Articles Join Table
CREATE TABLE IF NOT EXISTS incident_kb_articles (
    incident_id BIGINT NOT NULL,
    kb_article_id BIGINT NOT NULL,
    PRIMARY KEY (incident_id, kb_article_id),
    CONSTRAINT fk_incident_kb_incident FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE,
    CONSTRAINT fk_incident_kb_article FOREIGN KEY (kb_article_id) REFERENCES kb_articles(id) ON DELETE CASCADE
);

-- KB Optimization Indexes
CREATE INDEX IF NOT EXISTS idx_kb_article_revisions_article_id ON kb_article_revisions(article_id);
CREATE INDEX IF NOT EXISTS idx_incident_kb_articles_incident_id ON incident_kb_articles(incident_id);

