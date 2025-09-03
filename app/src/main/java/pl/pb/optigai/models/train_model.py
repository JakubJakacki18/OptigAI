import tensorflow as tf
from tensorflow.keras import layers
import os
import numpy as np
from model_creator import create_braille_cnn_model

# Krok 1: Wczytanie i przygotowanie danych z katalogu
# Twój zbiór danych nie ma podkatalogów, więc wczytujemy pliki ręcznie.

# Ustaw ścieżkę do folderu z danymi
data_dir = 'C:/Users/Hackie/PycharmProjects/OptigAI/app/src/main/res/values/Braille Dataset'

# Sprawdź, czy katalog istnieje, aby uniknąć błędów
if not os.path.exists(data_dir):
    print(f"Błąd: Nie znaleziono katalogu z danymi: {data_dir}")
    print("Upewnij się, że podałeś/aś poprawną ścieżkę do folderu z obrazami Braille'a.")
    exit()

# Parametry ładowania obrazów
image_size = (28, 28)

# Wczytywanie plików obrazów i etykiet
all_image_paths = [os.path.join(data_dir, fname) for fname in os.listdir(data_dir) if fname.endswith(('jpg', 'jpeg', 'png', 'gif'))]
all_labels = [fname.split('.')[0][0].lower() for fname in os.listdir(data_dir) if fname.endswith(('jpg', 'jpeg', 'png', 'gif'))]

# Tworzenie mapowania etykiet tekstowych na liczby
unique_labels = sorted(list(set(all_labels)))
label_to_index = {label: i for i, label in enumerate(unique_labels)}
num_classes = len(unique_labels)
labels = np.array([label_to_index[label] for label in all_labels])

print(f"Znaleziono {len(all_image_paths)} obrazów dla {num_classes} klas: {unique_labels}")

# Funkcja do wczytywania i przetwarzania obrazów
def load_and_preprocess_image(path):
    img = tf.io.read_file(path)
    img = tf.image.decode_jpeg(img, channels=1) # Dekodowanie jako obraz w skali szarości
    img = tf.image.resize(img, image_size)
    img = img / 255.0 # Normalizacja
    return img

# Tworzenie zbioru danych
path_ds = tf.data.Dataset.from_tensor_slices(all_image_paths)
image_ds = path_ds.map(load_and_preprocess_image, num_parallel_calls=tf.data.AUTOTUNE)
label_ds = tf.data.Dataset.from_tensor_slices(labels)
image_label_ds = tf.data.Dataset.zip((image_ds, label_ds))

# --- ZMIANA: Usunięcie podziału na zbiór treningowy i walidacyjny ---
# Cały zbiór danych jest teraz używany do treningu.
train_ds = image_label_ds
# -------------------------------------------------------------------

# Ustalenie rozmiaru partii (batch size)
batch_size = 32
train_ds = train_ds.shuffle(buffer_size=len(all_image_paths)).batch(batch_size).cache().prefetch(buffer_size=tf.data.AUTOTUNE)


# Krok 2: Utworzenie i skompilowanie modelu
input_shape = (*image_size, 1)
model = create_braille_cnn_model(input_shape, num_classes)
model.summary()

# Krok 3: Trenowanie modelu
print("\nRozpoczynanie treningu...")
# Zamiast validation_data używamy teraz validation_split
# W tym przypadku to jest 0, żeby użyć całego zbioru
history = model.fit(train_ds, epochs=10, validation_split=0.0)

# Krok 4: Zapisanie i konwersja modelu
model_name = "braille_recognition_model"
model.save(f"{model_name}.h5")
print(f"\nModel zapisany jako: {model_name}.h5")

# Konwersja modelu do formatu TFLite
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

# Zapisanie skonwertowanego modelu
with open(f"{model_name}.tflite", "wb") as f:
    f.write(tflite_model)
print(f"Model skonwertowany i zapisany jako: {model_name}.tflite")
