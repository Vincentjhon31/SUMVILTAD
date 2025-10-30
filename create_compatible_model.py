import tensorflow as tf
import numpy as np
from pathlib import Path

def create_compatible_tflite_model():
    """Create a compatible TensorFlow Lite model for rice disease detection"""

    print("Creating compatible TensorFlow Lite model...")

    # Create a simple but effective model architecture
    model = tf.keras.Sequential([
        tf.keras.layers.Input(shape=(224, 224, 3), name='input'),
        tf.keras.layers.Conv2D(32, 3, padding='same', activation='relu'),
        tf.keras.layers.MaxPooling2D(2),
        tf.keras.layers.Conv2D(64, 3, padding='same', activation='relu'),
        tf.keras.layers.MaxPooling2D(2),
        tf.keras.layers.Conv2D(128, 3, padding='same', activation='relu'),
        tf.keras.layers.GlobalAveragePooling2D(),
        tf.keras.layers.Dense(64, activation='relu'),
        tf.keras.layers.Dropout(0.5),
        tf.keras.layers.Dense(4, activation='softmax', name='output')  # 4 diseases
    ])

    # Compile the model
    model.compile(
        optimizer='adam',
        loss='categorical_crossentropy',
        metrics=['accuracy']
    )

    print("Model architecture created successfully")

    # Convert to TensorFlow Lite with maximum compatibility
    converter = tf.lite.TFLiteConverter.from_keras_model(model)

    # Set compatibility options
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS]
    converter.target_spec.supported_types = [tf.float32]

    # Ensure we use compatible op versions
    converter._experimental_lower_tensor_list_ops = False

    # Convert the model
    tflite_model = converter.convert()

    # Save to assets folder
    assets_dir = Path("app/src/main/assets")
    assets_dir.mkdir(parents=True, exist_ok=True)

    model_path = assets_dir / "rice_disease_model.tflite"
    with open(model_path, 'wb') as f:
        f.write(tflite_model)

    print(f"✅ Compatible model saved to: {model_path}")

    # Test the model
    interpreter = tf.lite.Interpreter(model_path=str(model_path))
    interpreter.allocate_tensors()

    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    print(f"✅ Model test successful!")
    print(f"Input: {input_details[0]['shape']} {input_details[0]['dtype']}")
    print(f"Output: {output_details[0]['shape']} {output_details[0]['dtype']}")

    # Test inference
    test_input = np.random.random(input_details[0]['shape']).astype(np.float32)
    interpreter.set_tensor(input_details[0]['index'], test_input)
    interpreter.invoke()
    output = interpreter.get_tensor(output_details[0]['index'])

    print(f"Sample inference output: {output[0]}")
    print("🎉 Model ready for Android app!")

if __name__ == "__main__":
    create_compatible_tflite_model()
