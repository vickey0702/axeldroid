/*
 *   Copyright 2012 by dragon 
 *   bolg:	http://blog.csdn.net/xidomlove
 *   mail:	fufulove2012@gmail.com
 *   File:      com_axeldroid_Axel.cpp
 *   Date:      2012-12-17下午10:58:17
 */
#include "com_axeldroid_Axel.h"
#include <string.h>
#define LOG_TAG "axeldroid.so" //自定义的变量，相当于logcat函数中的tag
#undef LOG
#include <android/log.h>   //#include <utils/Log.h>//在源码环境中，头文件的路径不同
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif
int gmain(int argc, char *argv[], JNIEnv *env, jobject obj,
		void (*setAxelJniInfo)(axel_t*, JNIEnv*, jobject));
#ifdef __cplusplus
}
#endif

/*
typedef struct tag_axel_list {
	axel_t* axel;
	tag_axel_list* next;
} axel_list;

axel_list* axels = NULL;
*/
//void addAxelObj(axel_t*);
//void removeAxelObj(axel_t*);
//更新axel下载进度
void setAxelJniInfo(axel_t* axel, JNIEnv *env, jobject obj);

//通知下载完成
void notifyFinish(void*);

//axel_t* find_my_axel(JNIEnv *,jobject obj);

JNIEXPORT void JNICALL Java_com_axeldroid_Axel_newTask(JNIEnv *env,
		jobject obj, jint connections, jstring filename, jobjectArray urls) {
	int argc = 6 + env->GetArrayLength(urls);
	LOGV("args length:%d", argc);
char* *argvs = new char*[argc];
	argvs[0] = "hello";
	argvs[1] = "-o";
	const char* cfn = env->GetStringUTFChars(filename, 0);
	char fn[1024] = { 0 };
	strcpy(fn, cfn);
	LOGV("output filename:%s", fn);

argvs[2] = fn;
	argvs[3] = "-n";
	char conn[10] = { 0 };
	sprintf(conn, "%d", (int) connections);
	argvs[4] = conn;
	argvs[5] = "-q";
	int i = 6;
	for (; i < argc; i++) {
		jstring str = (jstring) env->GetObjectArrayElement(urls, i - 6);
		const char* u = env->GetStringUTFChars(str, 0);
		int len = strlen(u) + 1;
		argvs[i] = new char[len];
		memcpy(argvs[i], u, len);
		
	LOGV("url NO%d:%s", i-5, argvs[i]);
}

//char *argvs[] = {"hello", "-o", "/mnt/sdcard/CZPAD/aa.war", "-n", "2", "-a", "http://localhost/CZHDP.war"};
gmain(7, argvs, env, obj, setAxelJniInfo);

for (i = 6; i < argc; i++) {
delete[] argvs[i];
}
delete[] argvs;

}

JNIEXPORT void JNICALL Java_com_axeldroid_Axel_axel_stop(JNIEnv *env, jobject obj,jlong paxel) {
	axel_t* axel=(axel_t*)paxel;
	notifyFinish(axel);
	axel->run=0;
}

void setAxelJniInfo(axel_t* axel, JNIEnv *env, jobject obj) {
//
jclass cls = env->GetObjectClass(obj); //env->FindClass("com.dragon.Greet");
axel->void_method_progress_id = env->GetMethodID(cls, "onProgress", "()V");
//	env->CallVoidMethod(obj, mid, 1);
axel->bytes_per_second_id = env->GetFieldID(cls, "bytes_per_second", "I");
axel->left_seconds_id = env->GetFieldID(cls, "left_seconds", "I");
axel->bytes_done_id = env->GetFieldID(cls, "bytes_done", "J");
axel->file_size_id = env->GetFieldID(cls, "file_size", "J");
env->SetLongField(obj, axel->file_size_id, axel->size);
axel->env = env;
axel->jobj = obj;
axel->progress = notifyProgress;
axel->notifyFinish = notifyFinish;
axel->run=1;

//LOGV("axel addr:%lld", (jlong)axel);
//LOGV("axel addr:%lld", (void*)axel);
env->SetLongField(obj, env->GetFieldID(cls, "pMyAxel", "J"),(jlong)axel);
//LOGV("axel addr:%lld", (jlong)axel);
notifyProgress(axel);
env->CallVoidMethod(obj, env->GetMethodID(cls, "onStart", "()V"));

//addAxelObj(axel);
LOGV("file size:%lld", axel->size);
//	env->SetIntField(obj, jid, 34);
//
}

JNIEXPORT void JNICALL Java_com_axeldroid_Axel_refreshProgress(JNIEnv *env, jobject obj,jlong paxel) {
	//LOGV("refresh progress req,axel addr:%lld",(void*)paxel);find_my_axel(env,obj)
		/* Calculate current average speed and finish_time		*/
//	axel_t* axel=(axel_t*)paxel;

	notifyProgress((void*)paxel);
//LOGV("refresh progress not found");
}

/*
axel_t* find_my_axel(JNIEnv *env,jobject obj){
LOGV("axel find,obj addr:%lld,axels addr:%lld",obj,axels);

	axel_list *p=axels;
while(p) {
if(env->IsSameObject(p->axel->jobj,obj)==JNI_TRUE) {
	LOGV("axel found");
	return p->axel;
}
p=p->next;
LOGV("axel find,obj addr:%lld,axels addr:%lld",obj,p);
}

return NULL;
}
*/
void notifyProgress(void* ax) {
	if(!ax)return;
axel_t* axel = (axel_t*) ax;
JNIEnv *env = axel->env;
double now=gettime();
axel->bytes_per_second = (int) ( (double) ( axel->bytes_done - axel->start_byte ) / ( now - axel->start_time ) );
axel->finish_time = (int) ( axel->start_time + (double) ( axel->size - axel->start_byte ) / axel->bytes_per_second );
env->SetIntField(axel->jobj, axel->bytes_per_second_id, axel->bytes_per_second);
env->SetIntField(axel->jobj, axel->left_seconds_id,
	axel->finish_time - now);
env->SetLongField(axel->jobj, axel->bytes_done_id, axel->bytes_done);
//	env->CallVoidMethod(axel->jobj, axel->void_method_progress_id);
}

void notifyFinish(void* ax) {
	if(!ax)return;
LOGV("FINISH");
axel_t* axel = (axel_t*) ax;
notifyProgress(ax);
jclass cls = axel->env->GetObjectClass(axel->jobj);
axel->env->SetIntField(axel->jobj, axel->env->GetFieldID(cls, "cost_seconds", "I"),(int)(gettime()-axel->start_time));
axel->env->CallVoidMethod(axel->jobj,
	axel->env->GetMethodID(cls, "onFinish", "()V"));
//removeAxelObj(axel);
}

/*
void addAxelObj(axel_t* axel) {
if (axels == NULL) {
axels = (axel_list*) malloc(sizeof(axel_list));
axels->next = NULL;
axels->axel = axel;
} else {
axel_list* p = (axel_list*) malloc(sizeof(axel_list));
p->next = axels;
p->axel = axel;
axels = p;
}
}

void removeAxelObj(axel_t* axel) {
axel_list *b, *p = axels;
if (p->axel == axel) {
free(p);
axels = NULL;
return;
}
while (p->axel != axel) {
b = p;
p = p->next;
}
b->next = p->next;
free(p);
}
*/
