<template>
    <v-card outlined class="mb-3" v-if="listing !== {}">
        <div class="overflow-hidden">
            <v-row no-gutters class="flex-nowrap disable-scrollbars" style="overflow-x: auto">
                <v-col v-for="(image, j) in image_urls" :key="j" cols="3">
                    <v-card flat tile class="d-flex">
                        <v-img
                            :src="image"
                            aspect-ratio="1"
                            :height="60"
                            class="grey lighten-2">
                            <template v-slot:placeholder>
                                <v-row
                                    class="fill-height ma-0"
                                    align="center"
                                    justify="center"
                                >
                                    <v-progress-circular indeterminate color="grey lighten-5"></v-progress-circular>
                                </v-row>
                            </template>
                        </v-img>
                    </v-card>
                </v-col>
            </v-row>
        </div>
        <v-row no-gutters align="center" justify="space-between" class="pa-6">
            <v-col cols="12" sm="8">
                <div>
                    <h3 class="title font-weight-regular">{{listing.name}}</h3>
                    <span class="caption grey--text">
                                      {{listing.city}}
                                      &bull;
                                      {{listing.sizeInM2}} m&sup2;
                                      &bull;
                                      {{$t('listing.item.pricePerMonth', [$options.filters.formatNumber(listing.pricePerMonth)])}}
                                  </span>
                </div>
                <div>
                    <!--features -->
                </div>
            </v-col>
            <v-col cols="12" sm="2" xl="1" class="text-right">
                <v-menu offset-y>
                    <template v-slot:activator="{ on }">
                        <v-btn icon v-on="on">
                            <v-icon>
                                mdi-dots-horizontal
                            </v-icon>
                        </v-btn>
                    </template>
                    <v-list>
                        <v-list-item @click="$emit('edit-listing', listing)" v-text="$t('listing.item.action.edit')">
                            Bearbeiten
                        </v-list-item>
                        <v-list-item @click="$emit('delete-listing', listing.id)">
                            <span class="red--text" v-text="$t('listing.item.action.delete')">Löschen</span>
                        </v-list-item>
                    </v-list>
                </v-menu>
                <v-tooltip bottom>
                    <template v-slot:activator="{ on }">
                        <v-btn icon v-on="on" @click="$emit('details-listing', listing.id)">
                            <v-icon>
                                mdi-chevron-right
                            </v-icon>
                        </v-btn>
                    </template>
                    <span v-text="$t('listing.item.action.details')">Details</span>
                </v-tooltip>
            </v-col>
        </v-row>
        <v-row no-gutters align="center"
               justify="space-between"
               class="grey lighten-4 px-4 py-2 cursor-pointer"
               :style="[(leasingSectionOpen) ? 'border-bottom: 1px solid #afafaf' : '']"
               @click="toggleItemLeasings">
            <v-col cols="9" sm="10">
                <p class="body-2 ma-0" v-text="$t('listing.item.leasing.title')">Buchungen für dieses Feld</p>
            </v-col>
            <v-col cols="3" sm="2" class="text-right">
                <v-btn icon small v-if="leasingSectionOpen && !itemLeasingsLoading" @click.stop="fetchItemLeasings">
                    <v-icon>
                        mdi-refresh
                    </v-icon>
                </v-btn>
                <v-progress-circular v-if="showItemLeasings && itemLeasingsLoading"
                                     indeterminate
                                     size="24"
                                     width="2"
                                     color="grey"></v-progress-circular>
                <v-btn icon>
                    <v-icon>
                        {{(showItemLeasings) ? 'mdi-chevron-up' : 'mdi-chevron-down'}}
                    </v-icon>
                </v-btn>
            </v-col>
        </v-row>
        <v-row no-gutters v-if="leasingSectionOpen">
            <v-col cols="12">
                <v-sheet class="grey lighten-3 pa-2" tile>
                    <v-row no-gutters align="center">
                        <v-col cols="12" sm="2">
                            <v-icon>mdi-filter-variant</v-icon>
                            <span v-if="$vuetify.breakpoint.mdAndUp || $vuetify.breakpoint.xsOnly" class="grey--text">Filter</span>
                        </v-col>
                        <v-col cols="auto">
                            <v-btn-toggle v-model="itemLeasingsFilters.leasingStatus" multiple @change="fetchItemLeasings">
                                <v-btn v-for="(item, i) in statuses"
                                       :key="'status'+i"
                                       outlined small
                                       :color="item.color"
                                       :value="item.status">{{$t(`leasing.status.${item.status}`)}}
                                </v-btn>
                            </v-btn-toggle>
                        </v-col>
                    </v-row>
                </v-sheet>
                <v-list v-if="itemLeasings.length > 0" two-line dense class="py-0">
                    <template v-for="(item, index) in itemLeasings">
                        <v-list-item :key="index">
                            <v-list-item-content>
                                <v-list-item-title>
                                    <span>{{item.user.lastName}}&nbsp;{{item.user.firstName}}</span>
                                    &nbsp;
                                    <v-chip label small disabled>{{$t(`leasing.status.${item.status}`)}}</v-chip>
                                </v-list-item-title>
                                <v-list-item-subtitle>
                                    <span class="grey--text" v-html="`${$t('leasing.period')}:`"></span>
                                    <span>{{item.from | formatDate('short')}} - {{item.to | formatDate('short')}}</span>
                                    &nbsp;&bull;&nbsp;
                                    <span class="grey--text" v-html="`${$t('leasing.price')}:`"></span>
                                    <span>{{listing.pricePerM2*listing.sizeInM2 | calcPeriodPrice(item.from, item.to) | formatNumber}} €</span>
                                </v-list-item-subtitle>
                            </v-list-item-content>
                            <v-list-item-action>
                                <v-menu bottom left :close-on-content-click="false">
                                    <template v-slot:activator="{ on }">
                                        <v-btn icon v-on="on">
                                            <v-icon>
                                                mdi-dots-horizontal
                                            </v-icon>
                                        </v-btn>
                                    </template>
                                    <v-card class="pa-3" max-width="150">
                                        <span class="caption grey--text font-weight-bold" v-text="$t('listing.item.leasing.updateStatus')">Status aktualisieren</span>
                                        <leasing-status-switcher v-model="item.status" @update-status="updateLeasingStatus(item, $event)"></leasing-status-switcher>
                                    </v-card>
                                </v-menu>
                            </v-list-item-action>
                        </v-list-item>
                    </template>
                </v-list>
                <v-sheet v-else class="pa-2 white" tile>
                    <p class="grey--text" v-text="$t('listing.item.leasing.noResults')">No results found</p>
                </v-sheet>
                <v-sheet class="grey lighten-3 pa-2" tile>
                    <v-row no-gutters align="center" justify="end">
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
                </v-sheet>
            </v-col>
        </v-row>
    </v-card>
</template>

<script lang="ts" src="./listingItem.component.ts"></script>

<style scoped lang="scss">
    .disable-scrollbars {
        scrollbar-width: none; /* Firefox */
        -ms-overflow-style: none; /* IE 10+ */
        &::-webkit-scrollbar {
            width: 0px;
            background: transparent; /* Chrome/Safari/Webkit */
        }
    }
</style>
