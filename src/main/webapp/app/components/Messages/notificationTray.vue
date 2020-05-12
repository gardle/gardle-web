<template>
    <v-menu offset-y :close-on-content-click='false' :disabled="notificationsExist()" v-model="open">
        <template v-slot:activator="{ on }">
            <v-badge overlap bordered color="green" :content="nrOfUnreadMessages"
                     :value="nrOfUnreadMessages !== 0" :offset-x="25" :offset-y="25">
                <v-btn icon v-on="on" v-on:click="opened()" class="ma-0 pa-0">
                    <v-icon v-if="notificationsExist()">
                        mdi-bell-outline
                    </v-icon>
                    <v-icon v-else>
                        mdi-bell
                    </v-icon>
                </v-btn>
            </v-badge>
        </template>
        <v-list class="notificationDropdown">
            <notification-tray-item v-for="(notification, i) in notificationList" :key="i"
                                    :notification="notification" v-on:selected="closeTray()">

            </notification-tray-item>
        </v-list>
    </v-menu>
</template>

<script lang="ts" src="./notificationTray.component.ts"></script>

<style scoped>
    .notificationDropdown {
        overflow-y: auto;
        max-height: 400px;
    }
</style>
