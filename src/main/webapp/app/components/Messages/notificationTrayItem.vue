<template>
    <v-list-item @click="navigateToSource()">
        <v-container class="ma-0 pa-0" :to="'/account/messages/thread/'+ notification.thread">
            <v-divider></v-divider>
            <v-row no-gutters align="center" class="py-2">
                <v-col cols="1">
                    <v-icon v-if="notification.type===userType">
                        mdi-email-outline
                    </v-icon>
                    <v-icon v-if="notification.type===leasingOpenType">
                        mdi-calendar-range-outline
                    </v-icon>
                    <v-icon v-if="notification.type===leasingCancelledType">
                        mdi-calendar-remove-outline
                    </v-icon>
                    <v-icon v-if="notification.type===leasingReservedType">
                        mdi-calendar-check-outline
                    </v-icon>
                    <v-icon v-if="notification.type===leasingRejectedType">
                        mdi-calendar-remove-outline
                    </v-icon>
                </v-col>
                <v-col cols="10 px-5">
                    <div>
                        <p v-if="notification.type===userType" class="caption ma-0">{{$t('message.notification.newMessage')}} {{notification.userFrom.firstName}} {{notification.userFrom.lastName}}.</p>
                        <p v-else-if="notification.type===leasingOpenType" class="caption ma-0">{{$t('message.notification.leasingOpen', {fieldName: notification.leasing.gardenField.name, firstName: notification.leasing.user.firstName, lastName: notification.leasing.user.lastName})}}.</p>
                        <p v-else-if="notification.type===leasingCancelledType" class="caption ma-0">{{$t('message.notification.leasingCancelled', {fieldName: notification.leasing.gardenField.name, firstName: notification.leasing.user.firstName, lastName: notification.leasing.user.lastName})}}.</p>
                        <p v-else-if="notification.type===leasingReservedType" class="caption ma-0">{{$t('message.notification.leasingReserved', {fieldName: notification.leasing.gardenField.name})}}.</p>
                        <p v-else-if="notification.type===leasingRejectedType" class="caption ma-0">{{$t('message.notification.leasingRejected', {fieldName: notification.leasing.gardenField.name})}}.</p>
                        <p class="caption grey--text ma-0">{{notification.createdDate | formatDate(date_time_format)}}</p>
                    </div>
                </v-col>
                <v-col cols="1">
                    <v-tooltip bottom v-if="notification.type===userType">
                        <template v-slot:activator="{ on }">
                            <v-btn icon v-on:click.stop="handleXClick()" v-on="on">
                                <v-icon color="grey" size="small">
                                    mdi-email-open
                                </v-icon>
                            </v-btn>
                        </template>
                        <span>{{$t('message.notification.markMessageRead')}}</span>
                    </v-tooltip>
                </v-col>
            </v-row>
        </v-container>
    </v-list-item>
</template>

<script lang="ts" src="./notificationTrayItem.component.ts"></script>

<style scoped>

</style>
