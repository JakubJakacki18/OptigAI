import numpy as np
import os

from tflite_model_maker.config import ExportFormat
from tflite_model_maker import model_spec
from tflite_model_maker import object_detector


from dotenv import load_dotenv
import tensorflow as tf
assert tf.__version__.startswith('2')
from pycocotools.coco import COCO

tf.get_logger().setLevel('ERROR')
from absl import logging
logging.set_verbosity(logging.ERROR)

load_dotenv()
path = os.getenv("PATH_TO_DATASET")
print(path)


gpus = tf.config.list_physical_devices("GPU")
print(f"GPUs: {len(gpus)}")
if gpus:
    try:
        for gpu in gpus:
            tf.config.experimental.set_memory_growth(gpu, True)
    except RuntimeError as e:
        print(e)

def get_data():
    train = object_detector.DataLoader.from_pascal_voc(
        images_dir=f'{path}/train/images',
        annotations_dir=f'{path}/train/annotations',
        label_map={1: 'Keys'},
    )

    valid = object_detector.DataLoader.from_pascal_voc(
        images_dir=f'{path}/valid/images',
        annotations_dir=f'{path}/valid/annotations',
        label_map={1: 'Keys'},
    )

    test = object_detector.DataLoader.from_pascal_voc(
        images_dir=f'{path}/test/images',
        annotations_dir=f'{path}/test/annotations',
        label_map={1: 'Keys'},
    )
    return train, valid, test

def test_dataset_size():
    train, valid, test = get_data()
    assert train.size > 64
    assert valid.size > 64
    assert test.size > 64
    print("Each dataset size is larger than 64")

train_data, valid_data, test_data = get_data()

print(f"Train size: {len(train_data)}")
print(f"Valid size: {len(valid_data)}")
print(f"Test size: {len(test_data)}")
test_dataset_size()
print("\n\n\n")


original_loadRes = COCO.loadRes

def patched_loadRes(self, resFile):
    if 'info' not in self.dataset:
        self.dataset['info'] = {
            "description": "Keys Dataset",
            "version": "1.0",
            "year": 2025,
            "contributor": "Name",
            "date_created": "2025-09-25"
        }
    if 'licenses' not in self.dataset:
        self.dataset['licenses'] = [{"id": 1, "name": "Custom", "url": ""}]
    return original_loadRes(self, resFile)

COCO.loadRes = patched_loadRes

spec = model_spec.get('efficientdet_lite4')
model = object_detector.create(train_data, model_spec=spec, batch_size=2, train_whole_model=True, validation_data=valid_data,epochs=20)
model.export(export_dir='.')
# model.evaluate(test_data,batch_size=2)
# model.model.evaluate(test_data.gen_tf_dataset(batch_size=2))
model.evaluate_tflite('model.tflite', test_data)

