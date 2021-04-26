'''
Upload furniture dataset to firebase.
'''
###################################################################################

import json
from typing import List
import firebase_admin
from firebase_admin import credentials, storage, firestore
from os import listdir
from os.path import isfile, join

###################################################################################

###################################################################################
#Firebase API needs.

STORAGE_PATH = "gs://homereality-7d0b8.appspot.com/"
CERTIFICATE = "/home/panchis/Desktop/HomeReality/FirebaseLoader/key.json"
DATA_PATH = "data"

###################################################################################

###################################################################################
#Utility functions.

def load_json(file_path):
    with open(file_path, 'r') as f:
        data = json.load(f)
    return data


def files_in_dir(mypath):
    return [join(mypath, f) for f in listdir(mypath) if isfile(join(mypath, f))]


def compare_records(doc1, doc2):
    return doc1.items() == doc2.items()

###################################################################################

###################################################################################

def main():

    #Connect to firebase.
    cred = credentials.Certificate(CERTIFICATE)
    app = firebase_admin.initialize_app(cred, {'storageBucket': STORAGE_PATH})
    store = firestore.client()
    doc_ref = store.collection(u'Furniture')
    docs = doc_ref.get()
    bucket = storage.bucket("homereality-7d0b8.appspot.com")

    #Load data for upload.
    path = DATA_PATH
    items_types = listdir(path)

    for item_type in items_types:
        path = DATA_PATH + '/' + item_type

        items_index = listdir(path)

        for index in items_index:
            item_files = listdir(path + '/' + index)

            images = [item_file for item_file in item_files if item_file.endswith('.jpg')]
            model = [item_file for item_file in item_files if item_file.endswith('.glb')][0]
            collection = [item_file for item_file in item_files if item_file.endswith('.json')][0]

            #Check if uploaded.
            exists = False

            item_data = load_json(path + '/' + index + '/' + collection)

            for doc in docs:
                if compare_records(doc._data, item_data):
                    exists = True

            if not exists:
                print("Add:" + model)
                #Add record.
                doc_ref.add(item_data)

                #Add model.
                fileName = path + '/' + index + '/' + model
                blob = bucket.blob('models/' + model)
                blob.upload_from_filename(fileName)
            
                #Add images.
                for image in images:
                    fileName = path + '/' + index + '/' + image
                    blob = bucket.blob('images/' + image)
                    blob.upload_from_filename(fileName)
            else:
                print("Skip:" + model)

###################################################################################


if __name__ == "__main__":
    main()
