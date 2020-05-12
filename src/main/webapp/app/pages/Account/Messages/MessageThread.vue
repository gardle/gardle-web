<template>
    <v-row v-if="userTo != null" class="fill-height align-start flex-column">
        <v-col :style="{flex: '0 0 auto', top: `${$vuetify.application.top}px`, zIndex: 1}" class="grey lighten-4 pa-0 position-sticky">
            <v-row justify="center" no-gutters>
                <v-col cols="11" sm="9" xl="7">
                    <v-toolbar elevation="0" class="transparent">
                        <v-btn icon @click="$router.back()">
                            <v-icon>
                                mdi-arrow-left
                            </v-icon>
                        </v-btn>
                        <v-toolbar-title>{{userTo.firstName}} {{userTo.lastName}}</v-toolbar-title>
                    </v-toolbar>
                </v-col>
            </v-row>
        </v-col>
        <v-col id="threadView" :style="threadStyle" class="pa-0" v-resize="updateThreadViewStyle" v-scroll:#threadView="onScrolling">
            <!--            Messages -->
            <v-row class="flex-column pa-4">
                <template v-for="(msg, i) in this.thread">
                    <v-col v-if="msg.type===userType" cols="auto" :key="i" :align-self="(msg.userFrom.id === myId) ? 'end' : 'start'" class="py-0">
                        <v-card outlined>
                            <v-card-text class="black--text" v-html="msg.content"></v-card-text>
                        </v-card>
                        <p class="caption grey--text px-2"
                           v-bind:class="{'end-text': msg.userFrom.id === myId, 'start-text': msg.userFrom.id !== myId}">
                            {{msg.createdDate |
                            formatDate('timestamp')}}</p>
                    </v-col>
                    <v-col v-else cols="6" :key="i" align-self="center" class="py-0">
                        <v-card outlined>
                            <v-card-text v-if="msg.type===leasingOpenType" class="grey--text" style="text-align: center"> {{$t('message.system.leasingOpen', {fieldName: msg.leasing.gardenField.name, firstName: msg.leasing.user.firstName, lastName: msg.leasing.user.lastName})}}</v-card-text>
                            <v-card-text v-if="msg.type===leasingCancelledType" class="grey--text" style="text-align: center"> {{$t('message.system.leasingCancelled', {fieldName: msg.leasing.gardenField.name, firstName: msg.leasing.user.firstName, lastName: msg.leasing.user.lastName})}}</v-card-text>
                            <v-card-text v-if="msg.type===leasingReservedType" class="grey--text" style="text-align: center"> {{$t('message.system.leasingReserved', {fieldName: msg.leasing.gardenField.name})}}</v-card-text>
                            <v-card-text v-if="msg.type===leasingRejectedType" class="grey--text" style="text-align: center"> {{$t('message.system.leasingRejected', {fieldName: msg.leasing.gardenField.name})}}</v-card-text>
                        </v-card>
                        <p class="caption grey--text px-2" style="text-align: center">{{msg.createdDate | formatDate('timestamp')}}</p>
                    </v-col>
                </template>
            </v-row>
        </v-col>
        <v-col style="flex: 0 0 auto; bottom: 0;" class="position-sticky pa-0">
            <v-sheet class="grey lighten-4 pt-5 px-5">
                <v-text-field outlined :label="$t('message.home.createLabel')"
                              v-model="msgDraft" :append-icon="(msgDraft !== '') ? 'mdi-send' : undefined"
                              @click:append="handleMsgSend" maxlength="2000" counter background-color="white"
                              @keyup.enter="handleMsgSend" :loading="msgSending" v-on:input="msgUpdate()">

                </v-text-field>
            </v-sheet>
        </v-col>
    </v-row>
</template>

<script lang="ts" src="./MessageThread.component.ts"></script>

<style scoped>
    #threadView {
        position: relative;
        overflow: hidden;
        overflow-y: auto;
    }

    .position-sticky {
        position: -webkit-sticky;
        position: sticky;
    }
</style>
