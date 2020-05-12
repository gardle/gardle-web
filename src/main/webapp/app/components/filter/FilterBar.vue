<template>
    <v-container fluid class="pa-0">
        <v-row class="flex-column pa-4" dense>
            <v-col>
                <v-text-field outlined type="text"
                              hide-details
                              :label="$t('filterBar.searchKeywords')"
                              v-model="filters.keywords"/>
            </v-col>
            <v-divider class="my-2"/>
            <v-col>
                <v-text-field outlined
                              :label="$t('filterBar.location.locationField')"
                              clearable
                              hide-details
                              @keyup.enter="setCoordinatesFromLocationString"
                              @click:clear="clearLocation"
                              v-model="filters.locationString">
                </v-text-field>
                <p class="caption grey--text py-1 pl-1 cursor-pointer mb-0" @click="handleGetMyLocation" v-text="$t('filterBar.location.myLocation')">Get my location</p>
            </v-col>
            <v-col v-if="filters.locationString !== null && filters.lat !== null">
                <v-subheader class="pl-0" v-text="$t('filterBar.location.locationRadius')">
                    How far from the location?
                </v-subheader>
                <v-slider
                    v-model="filters.radius"
                    class="align-center mt-8"
                    :max="100"
                    :min="5"
                    thumb-label="always"
                    thumb-size="30"
                    hide-details
                >
                    <template v-slot:thumb-label="{ value }">
                        <span style="font-size: 8px">{{value}}km</span>
                    </template>
                </v-slider>
            </v-col>
            <v-divider class="my-2"/>
            <v-col>
                <v-menu
                    ref="leasingTimeMenu"
                    v-model="showLeasingDateMenu"
                    :close-on-content-click="false"
                    :return-value.sync="filters.leasingTime"
                    transition="scale-transition"
                    offset-y
                    min-width="290px"
                >
                    <template v-slot:activator="{ on }">
                        <v-text-field
                            v-model="filters.leasingTime"
                            :label="$t('filterBar.leasingTime.calendarLabel')"
                            append-icon="mdi-calendar"
                            readonly
                            outlined
                            v-on="on"
                        ></v-text-field>
                    </template>
                    <v-date-picker v-model="filters.leasingTime" no-title scrollable range>
                        <v-spacer></v-spacer>
                        <v-btn text color="secondary" @click="resetLeasingTimeMenu()">Reset</v-btn>
                        <v-btn text color="primary" @click="showLeasingDateMenu = false">Cancel</v-btn>
                        <v-btn text color="primary" @click="$refs.leasingTimeMenu.save(filters.leasingTime)">OK</v-btn>
                    </v-date-picker>
                </v-menu>
            </v-col>
            <v-divider class="my-2"/>
            <v-col>
                <v-subheader v-html="$t('filterBar.price')">Price</v-subheader>
                <v-range-slider
                    v-model="filters.price"
                    :min="(filterBoundaries) ? filterBoundaries.minPrice : undefined"
                    :max="(filterBoundaries) ? filterBoundaries.maxPrice : undefined"
                    hide-details
                    thumb-label="always"
                    thumb-size="30"
                    class="align-center mt-5"
                ></v-range-slider>
            </v-col>
            <v-col>
                <v-subheader v-html="$t('filterBar.size')">Size</v-subheader>
                <v-range-slider
                    v-model="filters.size"
                    :min="(filterBoundaries) ? filterBoundaries.minSize : undefined"
                    :max="(filterBoundaries) ? filterBoundaries.maxSize : undefined"
                    hide-details
                    thumb-label="always"
                    thumb-size="24"
                    class="align-center mt-5"
                ></v-range-slider>
            </v-col>
            <v-divider class="my-2"/>
            <v-col>
                <v-subheader>Extras</v-subheader>
            </v-col>
            <v-col>
                <v-checkbox
                    v-model="filters.roofed"
                    hide-details
                    :label="$t('filterBar.roofed')">
                </v-checkbox>
                <v-checkbox
                    v-model="filters.water"
                    hide-details
                    :label="$t('filterBar.water')">
                </v-checkbox>
                <v-checkbox
                    v-model="filters.electricity"
                    hide-details
                    :label="$t('filterBar.electricity')">
                </v-checkbox>
                <v-checkbox
                    v-model="filters.high"
                    hide-details
                    :label="$t('filterBar.high')">
                </v-checkbox>
                <v-checkbox
                    v-model="filters.glassHouse"
                    hide-details
                    :label="$t('filterBar.glassHouse')">
                </v-checkbox>
            </v-col>
        </v-row>
        <portal to="navigationDrawerAppend">
            <v-divider/>
            <v-sheet class="white pa-3">
                <v-row no-gutters align="center" justify="space-between">
                    <v-tooltip top>
                        <template v-slot:activator="{ on }">
                            <v-btn icon @click="handleResetFilters" v-on="on">
                                <v-icon>
                                    mdi-cancel
                                </v-icon>
                            </v-btn>
                        </template>
                        <span>{{this.$t('filterBar.actions.resetFilters')}}</span>
                    </v-tooltip>

                    <v-btn text color="primary" @click="handleUpdateFilters"
                           v-text="$t('filterBar.navigationDrawerAppend.button.handleFilterUpdate')">
                        Update Filter
                    </v-btn>
                </v-row>
            </v-sheet>
        </portal>
    </v-container>
</template>

<script lang="ts" src="./FilterBar.component.ts">
</script>
