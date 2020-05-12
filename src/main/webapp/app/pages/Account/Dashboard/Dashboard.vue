<template>
    <v-row justify="center" no-gutters>
        <v-col cols="11" md="9" xl="7">
            <div class="pa-4">
                <div class="v-toolbar__title" v-html="$t('leasing.activeLeasings.title')"></div>
            </div>
            <v-data-iterator
                :items="(currentLeasings) ? currentLeasings.content : []"
                hide-default-footer>
                <template v-slot:default="{ items }">
                    <template v-for="(item, i) in items">
                        <v-card :key="i" class="mb-4" outlined hover>
                            <v-card-title>
                                <div>
                                    <h3 class="title">{{item.gardenField.name}}</h3>
                                    <p class="caption ma-0">
                                        <span v-html="$t('leasing.period')"></span>:
                                        <span class="grey--text">{{item.from | formatDate('short')}} - {{item.to | formatDate('short')}}</span>
                                    </p>
                                    <p class="caption">
                                        <span v-html="$t('leasing.fromUser')"></span>:
                                        <span class="grey--text">{{item.user.lastName}} {{item.user.firstName}}</span>
                                    </p>
                                </div>
                            </v-card-title>
                            <v-card-text>
                                <!-- Should contain a div spanning from the first to
                                     the last lease and evenly spreading events on the timeline -->
                                <v-sheet height="3" class="grey lighten-1" style="width: 100%">

                                </v-sheet>
                            </v-card-text>
                        </v-card>
                    </template>
                </template>
                <template v-slot:no-data>
                    <v-sheet class="text-center">
                        <v-icon size="128" class="mb-4 grey--text text--lighten-2">
                            mdi-calendar-clock
                        </v-icon>
                        <p class="grey--text" v-html="$t('leasing.activeLeasings.messages.noActiveLeasers')"></p>
                    </v-sheet>
                </template>
            </v-data-iterator>
            <v-divider></v-divider>
        </v-col>
        <v-col cols="11" md="9" xl="7">
            <div class="pa-4">
                <div class="v-toolbar__title" v-html="$t('leasing.leasingRequests.title')"></div>
            </div>
            <v-data-iterator
                :items="(requests) ? requests.content : []"
                hide-default-footer>
                <template v-slot:default="{ items }">
                    <template v-for="(item, i) in items">
                        <v-card :key="i" class="mb-4" outlined hover>
                            <v-row no-gutters align="center">
                                <v-col cols="12" sm="9" class="pa-4">
                                    <div>
                                        <h3 class="title">{{item.gardenField.name}}</h3>
                                        <p class="caption ma-0">
                                            <span v-html="$t('leasing.period')"></span>:
                                            <span class="grey--text">{{item.from | formatDate('short')}} - {{item.to | formatDate('short')}}</span>
                                        </p>
                                        <p class="caption">
                                            <span v-html="$t('leasing.fromUser')"></span>:
                                            <span class="grey--text">{{item.user.lastName}} {{item.user.firstName}}</span>
                                        </p>
                                    </div>
                                </v-col>
                                <v-col cols="12" sm="3">
                                    <v-row justify="end" no-gutters>
                                        <v-col>
                                            <v-btn block
                                                   text
                                                   large
                                                   tile
                                                   class="green white--text"
                                                   v-html="$t('leasing.leasingRequests.actions.accept')"
                                                   @click="acceptLeasingRequest(item.id, item.gardenField.id)">
                                                Accept
                                            </v-btn>
                                        </v-col>
                                        <v-col>
                                            <v-btn block
                                                   text
                                                   tile
                                                   class="error--text text--darken-2"
                                                   v-html="$t('leasing.leasingRequests.actions.reject')"
                                                   @click="rejectLeasingRequest(item.id, item.gardenField.id)">
                                                Reject
                                            </v-btn>
                                        </v-col>
                                    </v-row>
                                </v-col>
                            </v-row>
                        </v-card>
                    </template>
                </template>
                <template v-slot:footer v-if="requests && requests.totalElements > 5">
                    <i18n path="leasing.leasingRequests.messages.moreOpenRequests" tag="p" class="caption grey--text text--darken-1">
                        <template v-slot:action>
                            <router-link to="/">{{$t('leasing.leasingRequests.actions.clickHereForAll')}}</router-link>
                        </template>
                    </i18n>
                </template>
                <template v-slot:no-data>
                    <v-sheet class="text-center">
                        <v-icon size="128" class="mb-4 grey--text text--lighten-2">
                            mdi-inbox
                        </v-icon>
                        <p class="grey--text" v-html="$t('leasing.leasingRequests.messages.noOpenRequests')"></p>
                    </v-sheet>
                </template>
            </v-data-iterator>
        </v-col>
    </v-row>
</template>

<script lang="ts" src="./Dashboard.component.ts">
</script>
