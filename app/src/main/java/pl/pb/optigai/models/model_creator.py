import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers


def create_braille_cnn_model(input_shape, num_classes):
    """
    Tworzy i kompiluje model konwolucyjnej sieci neuronowej (CNN) do rozpoznawania obrazów.
    Model ten jest odpowiedni do zadań takich jak rozpoznawanie znaków Braille'a
    lub małych obiektów.

    Args:
        input_shape (tuple): Kształt wejściowego obrazu (wysokość, szerokość, kanały),
                             np. (28, 28, 1) dla obrazów w skali szarości.
        num_classes (int): Liczba klas do przewidywania (np. liczba znaków Braille'a).

    Returns:
        keras.Model: Skompilowany model Keras.
    """
    model = keras.Sequential([
        # Warstwa konwolucyjna 1:
        # 32 filtry, każdy o rozmiarze 3x3.
        # Aktywacja 'relu' (Rectified Linear Unit) wprowadza nieliniowość.
        layers.Conv2D(32, (3, 3), activation='relu', input_shape=input_shape),

        # Warstwa max pooling 1:
        # Zmniejsza wymiary obrazu, co pomaga zredukować złożoność obliczeniową
        # i sprawia, że model jest bardziej odporny na drobne zmiany w obrazie.
        layers.MaxPooling2D((2, 2)),

        # Warstwa konwolucyjna 2:
        # Kolejny zestaw filtrów (64), które uczą się bardziej złożonych cech.
        layers.Conv2D(64, (3, 3), activation='relu'),

        # Warstwa max pooling 2:
        # Ponownie zmniejsza wymiary.
        layers.MaxPooling2D((2, 2)),

        # Warstwa spłaszczająca (flatten):
        # Przekształca dane 2D z warstw konwolucyjnych w 1D wektor,
        # gotowy do podania na warstwy gęste (Dense).
        layers.Flatten(),

        # Warstwa gęsta (Dense) 1:
        # 128 neuronów, które uczą się klasyfikować na podstawie
        # cech wyodrębnionych przez warstwy konwolucyjne.
        layers.Dense(128, activation='relu'),

        # Warstwa wyjściowa:
        # Liczba neuronów równa liczbie klas.
        # Aktywacja 'softmax' zwraca rozkład prawdopodobieństwa dla każdej klasy.
        layers.Dense(num_classes, activation='softmax')
    ])

    # Kompilacja modelu:
    # 'Adam' to popularny optymalizator.
    # 'sparse_categorical_crossentropy' to funkcja straty odpowiednia dla
    # klasyfikacji wieloklasowej z etykietami w postaci liczb całkowitych.
    # 'accuracy' to metryka, którą będziemy monitorować podczas treningu.
    model.compile(optimizer='adam',
                  loss='sparse_categorical_crossentropy',
                  metrics=['accuracy'])

    return model


if __name__ == '__main__':
    # Przykładowe użycie:

    # Zdefiniuj kształt wejściowego obrazu (np. Braille'a 28x28 pikseli w skali szarości)
    # i liczbę klas (np. 26 liter + cyfry + znaki specjalne).
    braille_input_shape = (28, 28, 1)
    braille_num_classes = 64  # Przykładowa liczba klas

    # Utwórz model CNN dla Braille'a
    braille_model = create_braille_cnn_model(braille_input_shape, braille_num_classes)

    # Wyświetl podsumowanie struktury modelu
    braille_model.summary()

    # Model jest gotowy do trenowania (brakuje tylko danych i wywołania .fit())
    print("\nModel został pomyślnie utworzony. Gotowy do trenowania!")
