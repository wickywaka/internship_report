from collections import defaultdict
import json
import os
import copy

# todo:Inefficient implementation, can be made much efficient by sharing data
# todo: Check if numpy can be used here for fast array operations
class JsonProcessor(object):

    ########################### INIT #############################################
    def __init__(self, root, files_json):
        self.apks = defaultdict()   # list of apk data on each file, apk data is extracted from each json file. e.g,
                                    # methods in each file and its json string
        self.all_methods = dict() # dict[hash] = method-info
        self.freq_methods = defaultdict(list) # dict[freq] = [hash, hash, hash]
        self.method_freq = defaultdict(int)  # dict[hash] = freq
        self.files_json = files_json
        self.root = root
        self.methods_apk = dict()

        for file_name in self.files_json:
            self.apks[file_name] = dict()
            with open(self.root+file_name) as fd:
                self.apks[file_name]["json"] = json.load(fd)
            self.apks[file_name]["methods"] = self.get_apk_methods(file_name)
            self.methods_apk[file_name] = copy.deepcopy(self.apks[file_name]["methods"])
        self.all_methods = self.get_all_methods()
        self.method_freq = self.compute_method_freq()
        self.freq_methods = self.compute_freq_table_hashes()
        self.freq_methods_nb = self.compute_freq_table_nb()
        del self.apks
    ##############################################################################


    def get_apk_methods(self, file_name):
        """
        This method populates method portion of apks dictionray and also returns methods of an apk.
        :param file_name: name of json file
        :return: dictionary containing all methods in an apk, with hashes as keys and method data as value
        """
        json_apk = self.apks[file_name]["json"]
        hashes_dict = {}
        for class_data in json_apk["classes_info"].values():
            for method_data in class_data["methods_info"]:
                # if hashes_dict.get(method_data["hash"]) is None:
                #     hashes_dict[method_data["hash"]] = list()
                # hashes_dict[method_data["hash"]].append(method_data)
                hashes_dict[method_data["hash"]] = method_data
        self.apks[file_name]["methods"] = hashes_dict
        return hashes_dict

    def get_all_methods(self):
        """
        This method returns a dictionary containing all methods of all json files providide to the object
        :return: dictionry containing all methods inside all files
        """
        all_methods = dict()
        for file_name in self.files_json:
            all_methods.update(self.apks[file_name]["methods"])
        return all_methods

    def get_method_code(self, hash_):
        """
        This method returns the java source code of the method specified by the hash
        :param hash_:
        :return: string, java source code
        """
        if self.all_methods[hash_].get("java-source-code") is None:
            self.all_methods[hash_]["java-source-code"] = "No code available"
        return self.all_methods[hash_]["java-source-code"]

    def get_method_data(self, hash_):
        """
        This method return the method data info of a method specified by hash
        :param hash_: hash of the method
        :return: dictionary containing information about method
        """
        return self.all_methods[hash_]

    def compute_method_freq(self):
        """
        This method computes frequency of a method.
        :return: defauldiciontary containing method(hash)-> frequency data
        """
        d = defaultdict(int)
        for hash_ in self.all_methods.keys():
            for file_name in self.files_json:
                if hash_ in self.apks[file_name]["methods"].keys():
                    d[hash_] += 1
        return d

    def compute_freq_table_hashes(self):
        """
        This method groups the hashes into a dictionray in by the their frequency, e.g, methods having frequency of
        3 will be located in key positions 3 and the value will be all methods which has fequencey 3.
        :return: dictionary
        """
        d = defaultdict(list)
        if not self.method_freq:
            self.method_freq = self.compute_method_freq()
        for hash_, freq in self.method_freq.items():
            d[freq].append(hash_)
        return d

    def compute_freq_table_nb(self):
        d = self.compute_freq_table_hashes()
        d_ = dict()
        for key, value in d.items():
            d_[key] = len(value)
        return d_

    def get_methods_hashes_by_freq(self, freq):
        return self.freq_methods[freq]

    def get_methods_data_by_freq(self, freq):
        methods_data = dict()
        for hash_ in self.get_methods_hashes_by_freq(freq):
            methods_data[hash_] = self.get_method_data(hash_)
        return methods_data

    def get_methods_code_by_freq(self, freq):
        """
        Given the frequency, this method returns a dictionary of all java codes of all
        methods having that frequency.
        :param freq:
        :return: dict[hash] = java-source-code
        """
        methods_code = dict()
        for hash_ in self.get_methods_hashes_by_freq(freq):
            methods_code[hash_] = self.get_method_code(hash_)
        return methods_code


    def get_method_nb_by_freq(self, freq):
        return len(self.get_methods_hashes_by_freq(freq))


    def print_methods_code_by_freq(self, freq):
        #methods_code = dict()
        for hash_ in self.get_methods_hashes_by_freq(freq):
            print("##############################################################################################")
            print("Source code of method %s is:"%hash_)
            print(self.get_method_code(hash_))

    # todo: get_freq_by_hash,
