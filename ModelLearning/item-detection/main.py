import numpy as np
import os

from tflite_model_maker.config import ExportFormat
from tflite_model_maker import model_spec
from tflite_model_maker import object_detector

import tensorflow as tf
assert tf.__version__.startswith('2')

tf.get_logger().setLevel('ERROR')
from absl import logging
logging.set_verbosity(logging.ERROR)

def get_data():
    train = object_detector.DataLoader.from_pascal_voc(
        images_dir='/home/jakub/Datasets/Keys_dataset/train/images',
        annotations_dir='/home/jakub/Datasets/Keys_dataset/train/annotations',
        label_map={1: 'Keys'},
    )

    valid = object_detector.DataLoader.from_pascal_voc(
        images_dir='/home/jakub/Datasets/Keys_dataset/valid/',
        annotations_dir='/home/jakub/Datasets/Keys_dataset/valid/',
        label_map={1: 'Keys'},
    )

    test = object_detector.DataLoader.from_pascal_voc(
        images_dir='/home/jakub/Datasets/Keys_dataset/test/',
        annotations_dir='/home/jakub/Datasets/Keys_dataset/test/',
        label_map={1: 'Keys'},
    )
    return train, valid, test

train_data, valid_data, test_data = get_data()
spec = model_spec.get('efficientdet_lite4')
model = object_detector.create(train_data, model_spec=spec, batch_size=8, train_whole_model=True, validation_data=valid_data)
model.evaluate(test_data)
model.export(export_dir='.')
model.evaluate_tflite('model.tflite', test_data)

