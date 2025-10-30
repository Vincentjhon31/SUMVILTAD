
import torch
import torch.onnx
import onnx
from onnx_tf.backend import prepare
import tensorflow as tf
import numpy as np

def convert_pytorch_to_tflite(pytorch_model_path, output_path):
    """
    Convert PyTorch model to TensorFlow Lite
    """

    # Load PyTorch model
    device = torch.device('cpu')
    model = torch.load(pytorch_model_path, map_location=device)
    model.eval()

    # Create dummy input
    dummy_input = torch.randn(1, 3, 224, 224)

    # Convert to ONNX
    onnx_path = "temp_model.onnx"
    torch.onnx.export(
        model,
        dummy_input,
        onnx_path,
        export_params=True,
        opset_version=11,  # Use older opset for compatibility
        do_constant_folding=True,
        input_names=['input'],
        output_names=['output'],
        dynamic_axes={
            'input': {0: 'batch_size'},
            'output': {0: 'batch_size'}
        }
    )

    # Convert ONNX to TensorFlow
    onnx_model = onnx.load(onnx_path)
    tf_rep = prepare(onnx_model)
    tf_rep.export_graph("temp_model.pb")

    # Load TensorFlow model
    tf_model = tf.saved_model.load("temp_model.pb")

    # Convert to TensorFlow Lite
    converter = tf.lite.TFLiteConverter.from_saved_model("temp_model.pb")
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS]

    tflite_model = converter.convert()

    # Save TensorFlow Lite model
    with open(output_path, 'wb') as f:
        f.write(tflite_model)

    print(f"Model converted successfully: {output_path}")

if __name__ == "__main__":
    convert_pytorch_to_tflite(
        "app/sampledata/rice-disease-api/rice_disease_model_final.pth",
        "app/src/main/assets/rice_disease_model.tflite"
    )
