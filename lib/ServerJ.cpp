#include <jni.h>
#include <iostream>
#include <string>
#include "ServerJ.h"
using namespace std;

JNIEXPORT jstring JNICALL Java_ServerJ_getUppercase(JNIEnv *env, jclass cl, jstring inJNIStr) {

    const char *org = env->GetStringUTFChars(inJNIStr, NULL);
    if (NULL == org) return NULL;

    char letter;
    string result;

    int i = 0;
    int size = env->GetStringUTFLength(inJNIStr);

    while(i < size) {
        letter = org[i];
        if(islower(letter)) {
            letter = toupper(letter);
            result = result + letter;
        } else {
            result = result + letter;
            }
        i++;
        }

    return env->NewStringUTF(result.c_str());
}
