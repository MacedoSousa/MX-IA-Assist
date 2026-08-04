-- MX Initial Database Schema
-- Version: 001


CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


CREATE TABLE users (

    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    name VARCHAR(120) NOT NULL,

    email VARCHAR(255) UNIQUE NOT NULL,

    role VARCHAR(50) NOT NULL DEFAULT 'user',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);



CREATE TABLE workspaces (

    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    name VARCHAR(120) NOT NULL,

    type VARCHAR(50) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);



CREATE TABLE workspace_members (

    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    user_id UUID NOT NULL REFERENCES users(id),

    workspace_id UUID NOT NULL REFERENCES workspaces(id),

    permission VARCHAR(50) NOT NULL DEFAULT 'member'

);



CREATE TABLE projects (

    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    workspace_id UUID NOT NULL REFERENCES workspaces(id),

    name VARCHAR(150) NOT NULL,

    description TEXT,

    status VARCHAR(50) DEFAULT 'active',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);



CREATE TABLE memories (

    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    workspace_id UUID REFERENCES workspaces(id),

    content TEXT NOT NULL,

    category VARCHAR(100),

    importance INTEGER DEFAULT 1,

    source VARCHAR(100),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);



CREATE TABLE conversations (

    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    user_id UUID REFERENCES users(id),

    workspace_id UUID REFERENCES workspaces(id),

    summary TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);



CREATE TABLE agents (

    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    name VARCHAR(120) NOT NULL,

    purpose TEXT,

    permissions JSONB,

    active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);