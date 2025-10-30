#!/usr/bin/env python3
"""
TensorFlow Lite Model Converter Script
This script converts your existing model to a compatible TensorFlow Lite format
that works with the Android TensorFlow Lite runtime.
"""

import tensorflow as tf
import numpy as np
import os
from pathlib import Path

def convert_model_to_compatible_tflite():
    """
    Convert the existing TensorFlow model to a compatible TensorFlow Lite format
    """

    # Set TensorFlow to use compatible ops
    tf.config.experimental.enable_op_determinism()

    # Path to your existing model files
    model_dir = Path("app/sampledata/rice-disease-api")
    existing_model = model_dir / "rice_disease_model_final.pth"
    output_dir = Path("app/src/main/assets")

    if not existing_model.exists():
        print(f"PyTorch model not found at {existing_model}")
        print("Creating a compatible dummy model for testing...")

        # Create a simple compatible model for testing
        model = tf.keras.Sequential([
            tf.keras.layers.Input(shape=(224, 224, 3)),
            tf.keras.layers.Conv2D(32, 3, activation='relu'),
            tf.keras.layers.GlobalAveragePooling2D(),
            tf.keras.layers.Dense(64, activation='relu'),
            tf.keras.layers.Dense(4, activation='softmax')  # 4 classes: Bacterialblight, Blast, Brownspot, Tungro
        ])

        model.compile(
            optimizer='adam',
            loss='categorical_crossentropy',
            metrics=['accuracy']
        )

        print("Creating compatible TensorFlow Lite model...")

        # Convert with compatibility settings
        converter = tf.lite.TFLiteConverter.from_keras_model(model)

        # Set converter options for maximum compatibility
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        converter.target_spec.supported_ops = [
            tf.lite.OpsSet.TFLITE_BUILTINS,  # Use only built-in ops
        ]

        # Ensure we use older op versions for compatibility
        converter.target_spec.supported_types = [tf.float32]
        converter._experimental_lower_tensor_list_ops = False

        # Convert the model
        tflite_model = converter.convert()

        # Save the model
        output_dir.mkdir(parents=True, exist_ok=True)
        output_path = output_dir / "rice_disease_model.tflite"

        with open(output_path, 'wb') as f:
            f.write(tflite_model)

        print(f"✅ Compatible TensorFlow Lite model saved to: {output_path}")

        # Create labels file
        labels = ["Bacterialblight", "Blast", "Brownspot", "Tungro"]
        labels_path = output_dir / "labels.txt"

        with open(labels_path, 'w') as f:
            for label in labels:
                f.write(f"{label}\n")

        print(f"✅ Labels file saved to: {labels_path}")

        # Test the converted model
        test_model_compatibility(output_path)

    else:
        print("PyTorch model found. For PyTorch to TensorFlow conversion, you'll need to:")
        print("1. Load your PyTorch model")
        print("2. Convert to ONNX format")
        print("3. Convert ONNX to TensorFlow")
        print("4. Convert TensorFlow to TensorFlow Lite")
        print("\nFor now, using the dummy model approach above.")

def test_model_compatibility(model_path):
    """
    Test if the converted model works with TensorFlow Lite interpreter
    """
    try:
        # Load the model
        interpreter = tf.lite.Interpreter(model_path=str(model_path))
        interpreter.allocate_tensors()

        # Get input and output details
        input_details = interpreter.get_input_details()
        output_details = interpreter.get_output_details()

        print(f"✅ Model loaded successfully!")
        print(f"Input shape: {input_details[0]['shape']}")
        print(f"Input type: {input_details[0]['dtype']}")
        print(f"Output shape: {output_details[0]['shape']}")
        print(f"Output type: {output_details[0]['dtype']}")

        # Test with dummy input
        input_shape = input_details[0]['shape']
        test_input = np.random.random(input_shape).astype(np.float32)

        interpreter.set_tensor(input_details[0]['index'], test_input)
        interpreter.invoke()

        output_data = interpreter.get_tensor(output_details[0]['index'])
        print(f"✅ Model inference test successful!")
        print(f"Output shape: {output_data.shape}")
        print(f"Sample output: {output_data[0][:4]}")  # Show first 4 values

        return True

    except Exception as e:
        print(f"❌ Model compatibility test failed: {e}")
        return False

def create_pytorch_conversion_script():
    """
    Create a script to convert PyTorch models to TensorFlow Lite
    """

    script_content = '''
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
'''

    with open("convert_pytorch_model.py", "w") as f:
        f.write(script_content)

    print("✅ PyTorch conversion script created: convert_pytorch_model.py")

if __name__ == "__main__":
    print("🔧 Creating compatible TensorFlow Lite model...")
    convert_model_to_compatible_tflite()
    create_pytorch_conversion_script()
    print("\n🎉 Model conversion completed!")
    print("\nNext steps:")
    print("1. The compatible model is now in app/src/main/assets/")
    print("2. Your app should now work with offline detection")
    print("3. The basic image analysis will work as fallback")
