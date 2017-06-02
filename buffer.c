/* buffer.c */
#include <zconf.h>
#include <pthread.h>
#include <semaphore.h>
#include <stdlib.h>
#include "buffer.h"

buffer_item buffer[BUFFER_SIZE];
int in, out;

pthread_mutex_t mutex;
sem_t empty, full;

void init_buffer() {
    pthread_mutex_init(&mutex, NULL);
    sem_init(&empty, 0, BUFFER_SIZE);
    sem_init(&full, 0, 0);
    in = out = 0;
}

int insert_item(buffer_item item) {
    if (sem_wait(&empty) == 0 && pthread_mutex_lock(&mutex) == 0) {
        buffer[in++] = item;
        in %= BUFFER_SIZE;
        if (pthread_mutex_unlock(&mutex) == 0 && sem_post(&full) == 0) {
            return 0;
        }
    }

    return -1;
}

int remove_item(buffer_item *item) {
    if (sem_wait(&full) == 0 && pthread_mutex_lock(&mutex) == 0) {
        *item = buffer[out++];
        out %= BUFFER_SIZE;
        if (pthread_mutex_unlock(&mutex) == 0 && sem_post(&empty) == 0) {
            return 0;
        }
    }

    return -1;
}

