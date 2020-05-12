<template>
    <v-row justify="center">
        <v-col cols="12" class="grey lighten-4 mb-4 py-0">
            <v-row justify="center" no-gutters>
                <v-col cols="11" md="9">
                    <v-toolbar elevation="0" class="transparent">
                        <v-toolbar-title>{{$t('message.home.title')}}</v-toolbar-title>
                        <v-spacer/>
                    </v-toolbar>
                </v-col>
            </v-row>
        </v-col>
        <v-col cols="11" md="9" v-if=" messagePage && messagePage.totalPages !== 0">
            <template v-for="(message, i) in this.messagePage.content">
                <v-card outlined :key="i" class="mb-4" :to="'/account/messages/thread/'+message.thread"
                        :style="[isThreadUnopened(message) ? {'border-color': 'darkseagreen'} : {}]">
                    <v-card-title>
                        <h3 class="title" v-html="otherPersonInThread(message)"></h3>
                        <v-spacer/>
                        <v-icon v-if="isThreadUnopened(message)" style="color: darkseagreen">
                            mdi-email-mark-as-unread
                        </v-icon>
                    </v-card-title>
                    <v-card-text>
                        <div style="min-width: 0">
                            <span v-if="message.type === userType" class="caption grey--text mb-0 cutTextOnXS messagePreview">
                                {{isMsgFromMe(message) ? $t('message.home.me') : otherPersonInThread(message)}}: "{{message.content}}"
                            </span>
                            <span v-else-if="message.type === leasingOpenType" class="caption grey--text mb-0 cutTextOnXS messagePreview">
                               {{$t('message.system.leasingOpen', {fieldName: message.leasing.gardenField.name, firstName: message.leasing.user.firstName, lastName: message.leasing.user.lastName})}}
                            </span>
                            <span v-else-if="message.type === leasingCancelledType" class="caption grey--text mb-0 cutTextOnXS messagePreview">
                                {{$t('message.system.leasingCancelled', {fieldName: message.leasing.gardenField.name, firstName: message.leasing.user.firstName, lastName: message.leasing.user.lastName})}}
                            </span>
                            <span v-else-if="message.type === leasingReservedType" class="caption grey--text mb-0 cutTextOnXS messagePreview">
                                {{$t('message.system.leasingReserved', {fieldName: message.leasing.gardenField.name})}}
                            </span>
                            <span v-else-if="message.type === leasingRejectedType" class="caption grey--text mb-0 cutTextOnXS messagePreview">
                                {{$t('message.system.leasingRejected', {fieldName: message.leasing.gardenField.name})}}
                            </span>
                            <span class="caption grey--text mb-0 messagePreview"> &bull; {{message.createdDate | formatDate('timestamp')}}</span>
                        </div>
                    </v-card-text>
                </v-card>
            </template>
        </v-col>
        <v-col v-else>
            <v-sheet class="text-center">
                <v-icon size="128" class="mb-4 grey--text text--lighten-2">
                    mdi-email-outline
                </v-icon>
                <p class="grey--text">{{$t('message.home.noThreads')}}</p>
            </v-sheet>
        </v-col>
        <v-col v-if="messagePage.totalPages !== 0" cols="11" md="9">
            <v-pagination
                :length="this.messagePage.totalPages"
                @input="this.handleUpdatePageNumber"
                :disabled="this.messagePage.totalElements <= this.pageSize"
                v-model="this.messagePage.pageable.pageNumber + 1"
            ></v-pagination>
        </v-col>
    </v-row>
</template>

<script lang="ts" src="./Messages.component.ts"></script>

<style scoped>
    .scrollbar {
        position: relative;
        max-height: 80vh;
        height: 80vh;
        align-items: flex-start;
        overflow-y: auto;
    }

    .cutTextOnXS {
        text-overflow: ellipsis;
        overflow-x: hidden;
        max-width: 100%;
        white-space: nowrap;
    }

    .messagePreview {
        vertical-align: top;
        display: inline-block;
    }
</style>
