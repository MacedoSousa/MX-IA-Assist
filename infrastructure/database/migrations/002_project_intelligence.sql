-- MX Project Intelligence Layer
-- Version 002


CREATE TABLE project_technologies (

    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    project_id UUID NOT NULL REFERENCES projects(id),

    name VARCHAR(100) NOT NULL,

    category VARCHAR(50),

    version VARCHAR(50)

);



CREATE TABLE repositories (

    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    project_id UUID NOT NULL REFERENCES projects(id),

    provider VARCHAR(50),

    url TEXT,

    branch VARCHAR(100) DEFAULT 'main'

);



CREATE TABLE architecture_decisions (

    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    project_id UUID NOT NULL REFERENCES projects(id),

    title VARCHAR(200) NOT NULL,

    decision TEXT NOT NULL,

    reason TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);



CREATE TABLE project_documents (

    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    project_id UUID NOT NULL REFERENCES projects(id),

    name VARCHAR(200),

    type VARCHAR(50),

    location TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);