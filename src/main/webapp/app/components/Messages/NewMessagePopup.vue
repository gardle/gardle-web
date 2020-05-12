<template>
    <v-col cols="12">
        <v-dialog persistent
                  v-model="newMsgDialog"
                  transition="dialog-transition"
                  :fullscreen="$vuetify.breakpoint.xsOnly"
                  :width="$vuetify.breakpoint.smOnly ? '100vw' : '50vw'">
            <v-card>
                <v-card-title>
                    <span class="headline">{{$t('message.popup.title')}} {{owner.firstName}} {{owner.lastName}}</span>
                    <v-spacer></v-spacer>
                    <v-btn icon @click.stop="newMsgDialog = false">
                        <v-icon>
                            mdi-close
                        </v-icon>
                    </v-btn>
                </v-card-title>
                <v-divider></v-divider>
                <v-card-text>
                    <v-container>
                        <v-row>
                            <v-col cols="12">
                                <v-textarea class="white" outlined :label="$t('message.content')"
                                            :auto-grow="true" :loading="msgSending" counter maxlength="2000"
                                            v-model="msgDraft">

                                </v-textarea>
                            </v-col>
                        </v-row>
                    </v-container>
                </v-card-text>
                <v-divider></v-divider>
                <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn color="grey darken-1" text @click="newMsgDialog = false">{{$t('message.popup.cancel')}}</v-btn>
                    <v-btn class="my-2" depressed color="primary" @click="handleMsgSend" :disabled="msgDraft === ''">{{$t('message.popup.send')}}</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
    </v-col>
</template>

<script lang="ts" src="./NewMessagePopup.component.ts"></script>

<style scoped>

</style>
