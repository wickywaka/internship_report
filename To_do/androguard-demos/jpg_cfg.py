"""
This script making Control flow graphs of each method contained in an app. The input is an apk file hardcoded.
If it is throwing "in <string>' requires string as left operand, not int" error, then probably you are running it on
python 3.5, Use python 2.7
"""

# bug: Encodedmethod class has a method source() which is accessing one of decompiler class to show source code, but it
#       seems broken or decompiler is not chosen correctly

from androguard.core.bytecodes.apk import APK
from androguard.core.bytecodes.dvm import DalvikVMFormat
from androguard.core.analysis.analysis import Analysis
from androguard.core import bytecode
import os


directory = "/home/wra/workspace/cuckoodroid/utils/apps/"
file = "RGB.apk"


apk = APK(directory + file)
dvm = DalvikVMFormat(apk.get_dex()) # 
x = Analysis(dvm) # Analysis object, it also contains java code and probably edges too

name = apk.get_app_name() # todo: do string processing to remove nonstandard names


for i, method in enumerate(dvm.get_methods()): #method is EncodedmeMethod Object
    path_jpg = directory+"/"+name+ "_jpg"+"/"+str(method.get_class_name())+"/" # class name last character removed
    if not os.path.exists(path_jpg):
        os.makedirs(path_jpg)
    bytecode.method2jpg(path_jpg+str(method.get_name())+str(i)+".jpg", x.get_method(method)) # x.get_method(method), get MethodAnalysis object for "method"
    print(method.get_class_name() + method.get_name())

