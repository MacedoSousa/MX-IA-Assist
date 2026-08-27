import json

import torch


def main() -> None:
    available = torch.cuda.is_available()
    result = {
        "torchVersion": torch.__version__,
        "cudaAvailable": available,
        "cudaVersion": torch.version.cuda,
        "deviceName": torch.cuda.get_device_name(0) if available else None,
    }
    print(json.dumps(result, ensure_ascii=False))


if __name__ == "__main__":
    main()
