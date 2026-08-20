package com.macedxs.mx.api.v1.run;

public record ApprovalV1Request(String approvalNonce) {

    public ApprovalV1Request {
        if (approvalNonce == null || approvalNonce.isBlank()) {
            throw new IllegalArgumentException("Approval nonce is required");
        }
        approvalNonce = approvalNonce.trim();
    }
}
