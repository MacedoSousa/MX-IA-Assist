"""Emit a sanitized PyTorch/CUDA capability report for the local MX prototype."""

import json

try:
    import torch

    result = {
        "pytorchInstalled": True,
        "pytorchVersion": torch.__version__,
        "compiledCudaVersion": torch.version.cuda,
        "cudaAvailable": torch.cuda.is_available(),
        "deviceCount": torch.cuda.device_count() if torch.cuda.is_available() else 0,
    }
    if result["cudaAvailable"]:
        result["deviceName"] = torch.cuda.get_device_name(0)
except Exception as error:  # Inspection must never change the runtime.
    result = {
        "pytorchInstalled": False,
        "errorType": type(error).__name__,
        "errorMessage": str(error)[:160],
    }

print(json.dumps(result, ensure_ascii=False, sort_keys=True))
