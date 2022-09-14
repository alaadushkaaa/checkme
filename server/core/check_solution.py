import os
import sys
import shutil
import importlib
import pymongo
from dotenv import load_dotenv, find_dotenv
from bson.objectid import ObjectId 

def main():
    load_dotenv(find_dotenv())

    check_id, task_id = sys.argv[1:3]
    sys.path.append(os.path.join('/tasks', task_id))
    working_dir = os.path.join('/checks', check_id)
    os.mkdir(working_dir)
    os.chdir(working_dir)

    client = pymongo.MongoClient(os.environ.get("MONGODB_URL"))
    database = client.checkme
    task = database.tasks.find_one({'_id': ObjectId(task_id)})

    result = {}
    criterions = task['criterions']
    if 'prepare' in criterions:
        execute_file('prepare')
        criterions.pop('prepare')
    for criterion, data in criterions.items():
        if 'test' not in data.keys():
            continue
        os.chdir(working_dir) 
        result[criterion] = {}
        if(execute_file(data['test'])):
            result[criterion]['score'] = data['score']
            result[criterion]['message'] = data['description']
        else:
            result[criterion]['score'] = 0
            result[criterion]['message'] = data['message']

    database.checks.update_one({'_id': ObjectId(check_id)}, {'$set': { 'result': result, 'status': 'Проверено' }})

def execute_file(filename) -> bool:
    module = importlib.import_module(filename.rstrip('.py'))
    test = getattr(module, 'main')
    try:
        return test(sys.argv[3:])
    except Exception as e:
        print(e)
        return False

if __name__ == '__main__':
    main()
