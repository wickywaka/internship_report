"""
Demo: multiple apks to json format

"""
from andropack.JsonProcessor import JsonProcessor
import os


# Get json file list in current directory
jsons = []

print("[+] Malwares to be processed:")
for root, folders, files in os.walk("/home/wra/workspace/apks_lukas/sonicspy/"):
    i=1;
    for file in files:
        if file.endswith(".json") and not file.endswith("hashes.json"):
            print("\t"+str(i) + ": " +file.replace(".json",""))
            i=i+1
            jsons.append(file)
    print("[+] Processing JSON Data")
    jp = JsonProcessor(root, jsons)
    print("Done.")

#hash_ = jp.freq_methods[3][0]

#print(jp.get_method_code(hash_))


#Open ipython inteactive shell
#from IPython import embed
#embed()
