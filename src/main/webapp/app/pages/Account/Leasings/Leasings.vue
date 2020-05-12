<template>
    <v-row justify="center" align="start">
        <v-col cols="12" class="py-0 grey lighten-4">
            <v-row justify="center" no-gutters>
                <v-col cols="11" md="9" xl="7">
                    <v-toolbar elevation="0" class="transparent">
                        <v-toolbar-title>{{$t('leasing.title')}}</v-toolbar-title>
                        <v-spacer/>
                    </v-toolbar>
                </v-col>
            </v-row>
        </v-col>
        <v-col cols="12" md="9" xl="7">
            <v-row no-gutters v-if="leasings.length > 0">
                <template v-for="(item, i) in leasings">
                    <v-col cols="12" sm="6" lg="4" :key="i" class="px-1 py-1">
                        <v-card outlined :class="[(isLeasingCurrent(item.from, item.to)) ? 'info-border' : '']">
                            <v-list-item three-line>
                                <v-list-item-content>
                                    <span class="overline grey--text" v-text="$t('leasing.item.bookingFor')">{{$t('leasing.item.bookingFor')}}</span>
                                    <v-list-item-title class="title mb-1">{{item.gardenField.name}}</v-list-item-title>
                                    <v-list-item-subtitle class="caption grey--text">
                                        {{isLeasingInFuture(item.from) ? $t('leasing.item.period.future',
                                        [$options.filters.humanizeDateDistance(item.from)]) :
                                        hasLeasingEnded(item.to) ? $t('leasing.item.period.past',
                                        [$options.filters.humanizeDateDistance(item.to)]) :
                                        $t('leasing.item.period.current',
                                        [$options.filters.humanizeDateDistance(item.from)])}}
                                    </v-list-item-subtitle>
                                </v-list-item-content>
                                <v-list-item-action>
                                    <v-chip class="light-blue darken-4 white--text mb-1" label small
                                            v-if="isLeasingCurrent(item.from, item.to)"
                                            v-text="$t('leasing.item.ongoing')">Ongoing
                                    </v-chip>
                                    <v-chip label disabled small class="mb-2">{{$t(`leasing.status.${item.status}`)}}
                                    </v-chip>
                                </v-list-item-action>
                            </v-list-item>

                            <v-card-actions>
                                <v-spacer/>
                                <v-btn text small @click="showLeasingDetails(item)" v-text="$t('leasing.item.action.details')">
                                    Details
                                </v-btn>
                                <v-btn v-if="!isLeasingCurrent(item.from, item.to) && isLeasingCancellable(item)"
                                       small class="red--text text--accent-1" text @click="cancelLeasing(item)" v-text="$t('leasing.item.action.cancel')">
                                    Kündigen
                                </v-btn>
                            </v-card-actions>
                        </v-card>
                    </v-col>
                </template>
                <v-col cols="12" v-if="hasPagination" class="mt-4">
                    <v-row no-gutters align="center" justify="end">
                        <v-col cols="auto" class="text-right">
                            <span class="subtitle-2 grey--text">Page {{leasingPage.pageNumber+1}} of {{totalLeasingPages}}</span>
                        </v-col>
                        <v-col cols="2" class="text-right">
                            <v-btn small icon outlined class="grey lighten-2" @click="prevPage" :disabled="isFirstPage">
                                <v-icon>
                                    mdi-chevron-left
                                </v-icon>
                            </v-btn>
                            <v-btn small icon outlined class="grey lighten-2" @click="nextPage" :disabled="isLastPage">
                                <v-icon>
                                    mdi-chevron-right
                                </v-icon>
                            </v-btn>
                        </v-col>
                    </v-row>
                </v-col>
            </v-row>
            <v-row no-gutters v-else>
                <v-col cols="12">
                    <v-sheet class="pa-5 text-center" tile>
                        <v-icon size="128" class="mb-4 grey--text text--lighten-2">
                            mdi-map
                        </v-icon>
                        <p class="grey--text" v-html="$t('leasing.messages.noActiveLeasings')">
                        </p>
                    </v-sheet>
                </v-col>
            </v-row>
        </v-col>

        <v-dialog v-if="$vuetify.breakpoint.mdAndUp" max-width="500"
                  v-model="detailLeasingOpen">
            <v-card>
                <portal-target name="bigScreenDetail">
                </portal-target>
            </v-card>
        </v-dialog>
        <v-bottom-sheet v-if="$vuetify.breakpoint.smAndDown" v-model="detailLeasingOpen">
            <v-card tile min-height="250">
                <portal-target name="smallScreenDetail">
                </portal-target>
            </v-card>
        </v-bottom-sheet>

        <portal :to="$vuetify.breakpoint.mdAndUp ? 'bigScreenDetail' : 'smallScreenDetail'">
            <v-sheet v-if="!selectedLeasing">
                Please select a leasing in order to view its details
            </v-sheet>
            <v-sheet v-else>
                <v-list-item two-line>
                    <v-list-item-content>
                        <span class="overline grey--text" v-text="$t('leasing.item.bookingFor')">Buchung für</span>
                        <v-list-item-title class="title mb-1">{{selectedLeasing.gardenField.name}}</v-list-item-title>
                    </v-list-item-content>
                    <v-list-item-action>
                        <v-chip v-if="isLeasingCurrent(selectedLeasing.from, selectedLeasing.to)"
                                class="light-blue darken-4 white--text mb-1" label small
                                v-text="$t('leasing.item.ongoing')">Ongoing
                        </v-chip>
                        <v-chip class="mb-2" disabled label small>{{$t(`leasing.status.${selectedLeasing.status}`)}}</v-chip>
                    </v-list-item-action>
                </v-list-item>
                <!--                    maybe a map view here?-->
                <v-card-text class="grey lighten-5">
                    <div>
                        <v-subheader v-text="$t('leasing.detail.title')"></v-subheader>
                        <p class="mb-1">
                            <span class="font-weight-medium" v-text="$t('leasing.detail.period')"></span>&emsp;
                            {{selectedLeasing.from | formatDate('short')}} - {{selectedLeasing.to |
                            formatDate('short')}} ({{$t('leasing.detail.duration')}}: {{selectedLeasing.from
                            | humanizeDateInterval(selectedLeasing.to)}})
                        </p>
                        <p class="mb-1">
                            <span class="font-weight-medium" v-text="$t('leasing.detail.area')"></span>&emsp;&nbsp;
                            {{selectedLeasing.gardenField.sizeInM2 | formatNumber}} m&sup2;
                        </p>
                        <p class="mb-1">
                            <span class="font-weight-medium" v-text="$t('leasing.detail.pricePerDay')"></span>&emsp;
                            {{selectedLeasing.gardenField.sizeInM2 * selectedLeasing.gardenField.pricePerM2 | formatNumber}} €
                        </p>
                        <p class="mb-1">
                            <span class="font-weight-medium" v-text="$t('leasing.detail.priceTotal')"></span>&emsp;
                            {{selectedLeasing.gardenField.sizeInM2 * selectedLeasing.gardenField.pricePerM2 |
                            calcPeriodPrice(selectedLeasing.from, selectedLeasing.to) |
                            formatNumber }} €
                        </p>
                    </div>
                </v-card-text>
                <v-card-actions>
                    <v-spacer/>
                    <v-btn text small class="grey--text" :to="`/fields/${selectedLeasing.gardenField.id}`" v-text="$t('leasing.detail.action.moreDetails')">
                    </v-btn>
                </v-card-actions>
            </v-sheet>
        </portal>
    </v-row>
</template>

<script lang="ts" src="./Leasings.component.ts"></script>

<style scoped>
    .info-border {
        border-color: #01579B !important;
        background-color: #e7f3ff !important;
    }
</style>
