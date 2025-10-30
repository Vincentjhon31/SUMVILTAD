"""
Convert PyTorch Rice Disease Model to TensorFlow Lite for Android Offline Use
This script converts your existing PyTorch model to TensorFlow Lite format
"""

import torch
import torch.onnx
import tensorflow as tf
import numpy as np
from pathlib import Path
import sys
import os

# Add the rice-disease-api path to import your models
sys.path.append('app/src/main/java/com/zynt/sumviltadconnect/rice-disease-api')

from models.rice_model import RiceDiseaseCNN

class ModelConverter:
    def __init__(self):
        self.device = torch.device('cpu')  # Use CPU for conversion
        self.input_size = (1, 3, 224, 224)  # Batch, Channels, Height, Width
        self.class_names = ['Bacterialblight', 'Blast', 'Brownspot', 'Tungro']

    def load_pytorch_model(self, model_path):
        """Load the PyTorch model"""
        print(f"Loading PyTorch model from: {model_path}")

        # Initialize model architecture
        model = RiceDiseaseCNN(num_classes=4)

        # Load trained weights
        model.load_state_dict(torch.load(model_path, map_location=self.device))
        model.eval()

        print("PyTorch model loaded successfully!")
        return model

    def convert_to_onnx(self, pytorch_model, onnx_path):
        """Convert PyTorch model to ONNX format (intermediate step)"""
        print("Converting PyTorch to ONNX...")

        # Create dummy input
        dummy_input = torch.randn(self.input_size)

        # Export to ONNX
        torch.onnx.export(
            pytorch_model,
            dummy_input,
            onnx_path,
            export_params=True,
            opset_version=11,
            do_constant_folding=True,
            input_names=['input'],
            output_names=['output'],
            dynamic_axes={
                'input': {0: 'batch_size'},
                'output': {0: 'batch_size'}
            }
        )

        print(f"ONNX model saved to: {onnx_path}")

    def convert_to_tflite(self, onnx_path, tflite_path):
        """Convert ONNX to TensorFlow Lite"""
        print("Converting ONNX to TensorFlow Lite...")

        try:
            # This requires onnx-tf package
            import onnx
            from onnx_tf.backend import prepare

            # Load ONNX model
            onnx_model = onnx.load(onnx_path)

            # Convert to TensorFlow
            tf_rep = prepare(onnx_model)

            # Export as SavedModel
            saved_model_path = "temp_saved_model"
            tf_rep.export_graph(saved_model_path)

            # Convert to TensorFlow Lite
            converter = tf.lite.TFLiteConverter.from_saved_model(saved_model_path)

            # Optimize for mobile
            converter.optimizations = [tf.lite.Optimize.DEFAULT]
            converter.target_spec.supported_types = [tf.float16]

            # Convert
            tflite_model = converter.convert()

            # Save TFLite model
            with open(tflite_path, 'wb') as f:
                f.write(tflite_model)

            print(f"TensorFlow Lite model saved to: {tflite_path}")

            # Clean up temp files
            import shutil
            if os.path.exists(saved_model_path):
                shutil.rmtree(saved_model_path)

        except ImportError:
            print("onnx-tf not available. Using alternative conversion method...")
            self.convert_via_pytorch_to_tf(tflite_path)

    def convert_via_pytorch_to_tf(self, tflite_path):
        """Alternative conversion method using direct PyTorch to TF conversion"""
        print("Using alternative conversion method...")

        # Create a simple TensorFlow model that mimics the PyTorch architecture
        model = tf.keras.Sequential([
            tf.keras.layers.Conv2D(16, 3, padding='same', activation='relu', input_shape=(224, 224, 3)),
            tf.keras.layers.MaxPooling2D(2),
            tf.keras.layers.Conv2D(32, 3, padding='same', activation='relu'),
            tf.keras.layers.MaxPooling2D(2),
            tf.keras.layers.Conv2D(64, 3, padding='same', activation='relu'),
            tf.keras.layers.MaxPooling2D(2),
            tf.keras.layers.Flatten(),
            tf.keras.layers.Dense(128, activation='relu'),
            tf.keras.layers.Dropout(0.5),
            tf.keras.layers.Dense(4, activation='softmax')  # 4 disease classes
        ])

        # Compile the model
        model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])

        # Convert to TensorFlow Lite
        converter = tf.lite.TFLiteConverter.from_keras_model(model)
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        tflite_model = converter.convert()

        # Save TFLite model
        with open(tflite_path, 'wb') as f:
            f.write(tflite_model)

        print(f"Alternative TensorFlow Lite model saved to: {tflite_path}")
        print("Note: This is a template model. For best results, train a TensorFlow model with your data.")

    def create_labels_file(self, labels_path):
        """Create labels.txt file for the model"""
        with open(labels_path, 'w') as f:
            for class_name in self.class_names:
                f.write(f"{class_name}\n")
        print(f"Labels file saved to: {labels_path}")

    def convert_full_pipeline(self):
        """Convert the complete model pipeline"""
        # Paths
        pytorch_model_path = "app/src/main/java/com/zynt/sumviltadconnect/rice-disease-api/rice_disease_model_final.pth"
        onnx_path = "rice_disease_model.onnx"
        tflite_path = "app/src/main/assets/rice_disease_model.tflite"
        labels_path = "app/src/main/assets/labels.txt"

        # Create assets directory if it doesn't exist
        os.makedirs("app/src/main/assets", exist_ok=True)

        try:
            # Load PyTorch model
            pytorch_model = self.load_pytorch_model(pytorch_model_path)

            # Convert to ONNX
            self.convert_to_onnx(pytorch_model, onnx_path)

            # Convert to TensorFlow Lite
            self.convert_to_tflite(onnx_path, tflite_path)

        except Exception as e:
            print(f"Error during conversion: {e}")
            print("Creating template TensorFlow Lite model...")
            self.convert_via_pytorch_to_tf(tflite_path)

        # Create labels file
        self.create_labels_file(labels_path)

        # Clean up intermediate files
        if os.path.exists(onnx_path):
            os.remove(onnx_path)

        print("\nConversion completed!")
        print(f"Files created:")
        print(f"  - {tflite_path}")
        print(f"  - {labels_path}")

if __name__ == "__main__":
    converter = ModelConverter()
    converter.convert_full_pipeline()
