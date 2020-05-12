<template>
    <v-row justify="center">
        <v-container fluid pa-0>
            <v-row class="listing-detail--masthead" no-gutters>
                <v-container class="align-content-center pa-4 pt-0">
                    <v-row justify="center" no-gutters>
                        <v-col cols="11" xl="8" class="pt-0">
                            <v-row v-if="fieldImages && fieldImages.length > 0">
                                <v-col cols="12">
                                    <image-gallery v-bind:images="fieldImages" v-bind:thumbnails="fieldThumbnails"/>
                                </v-col>
                            </v-row>
                            <v-row v-if="gardenField" class="d-flex justify-space-between align-content-stretch">
                                <v-col cols="12" lg="8">
                                    <v-card class="py-3 px-5">
                                        <v-row class="d-flex flex-wrap justify-space-between mx-0 mb-2 mt-2">
                                            <v-col class="pt-0 pl-0 flex-grow-1 flex-shrink-1">
                                                <h1 class="display-1 grey--text text--darken-3 fill-width">{{name}}</h1>
                                                <h2 class="title grey--text text--darken-1">{{city}}</h2>
                                            </v-col>
                                            <v-col cols="12" md="auto" class="pa-0">
                                                <v-card @click.stop="openMsgPopup()" :disabled="amIOwner()"
                                                        class="ma-0 px-3">
                                                    <v-row align="center" class="flex-wrap">
                                                        <v-col cols="auto">
                                                            <v-icon left class="d-inline ma-0">
                                                                mdi-message-plus
                                                            </v-icon>
                                                        </v-col>
                                                        <v-col>
                                                            <p class="grey--text text--darken-3 ma-0">
                                                                {{fullOwnerName}}
                                                            </p>
                                                        </v-col>
                                                    </v-row>
                                                </v-card>
                                            </v-col>
                                            <new-message-popup :owner="gardenField.owner"/>
                                        </v-row>
                                        <v-row class="d-flex flex-wrap justify-space-around">
                                            <template v-for="(n, i) in fieldFeatures">
                                                <v-col :key="i"
                                                       v-if="n.value && n.value !== false"
                                                       class="pa-0"
                                                >
                                                    <v-card outlined class="mx-3 my-1 px-2 py-1">
                                                        <v-row align="center" dense class="flex-nowrap">
                                                            <v-col cols="auto">
                                                                <v-icon>
                                                                    {{n.icon}}
                                                                </v-icon>
                                                            </v-col>
                                                            <v-col>
                                                                <span v-if="n.type === Boolean">{{n.text}}</span>
                                                                <v-row v-if="n.type === Number" dense class="flex-nowrap">
                                                                    <span class="mr-1">{{n.text}}:</span>
                                                                    <span class="font-weight-bold">{{n.value}}</span>
                                                                    <span v-if="n.unit" class="font-weight-bold">{{n.unit}}</span>
                                                                </v-row>
                                                            </v-col>
                                                        </v-row>
                                                    </v-card>
                                                </v-col>
                                            </template>
                                            <v-col class="pa-0">
                                                <v-card outlined class="mx-3 my-1 px-2 py-1 info lighten-2">
                                                    <v-row align="center" dense class="flex-nowrap">
                                                        <v-col cols="auto">
                                                            <v-icon>
                                                                mdi-currency-eur
                                                            </v-icon>
                                                        </v-col>
                                                        <v-col>
                                                                <span class="d-inline grey--text text--darken-3">
                                                                    <span>{{$t('listing.item.pricePerMonth',
                                                                        [$options.filters.formatNumber(gardenField.pricePerMonth)])}}</span>
                                                                </span>
                                                        </v-col>
                                                    </v-row>
                                                </v-card>
                                            </v-col>
                                        </v-row>
                                        <v-row>
                                            <v-col cols="12" class="mt-2">
                                                <p>{{description}}</p>
                                            </v-col>
                                        </v-row>
                                    </v-card>
                                </v-col>
                                <v-col cols="auto" class="mx-md-2 mx-auto">
                                    <v-card>
                                        <v-date-picker
                                            v-model="bookingRange"
                                            :allowed-dates="allowedDates"
                                            range
                                            no-title
                                            class="elevation-0"
                                            :locale="locale"
                                        >
                                        </v-date-picker>
                                        <v-card-text v-if="selectedPrice" class="pa-0 text-right">
                                            <v-divider/>
                                            <p class="px-3">{{$t('listing.common.form.hint.priceTotal', [$options.filters.formatNumber(selectedPrice)])}}</p>
                                        </v-card-text>
                                        <v-card-actions>
                                            <v-spacer/>
                                            <v-btn @click="bookingRange=[]"
                                                   text
                                                   class="grey--text text--lighten-1"><span>{{$t('listing.common.form.button.reset')}}</span>
                                            </v-btn>
                                            <v-btn @click="bookField"
                                                   :disabled="bookingRange === [] || bookingRange.length < 2"
                                                   depressed
                                                   class="green lighten-2 green--text text--darken-3">
                                                {{$t('listing.common.form.button.book')}}
                                            </v-btn>
                                        </v-card-actions>
                                    </v-card>
                                </v-col>
                                <v-col class="flex-grow-1 flex-shrink-0">
                                    <v-card height="100%" min-height="220px" min-width="250px">
                                        <Map
                                                :latitude="gardenField.latitude"
                                                :longitude="gardenField.longitude"/>
                                    </v-card>
                                </v-col>
                            </v-row>
                        </v-col>
                    </v-row>
                </v-container>
            </v-row>
        </v-container>
    </v-row>
</template>

<script lang="ts" src="./ListingDetail.component.ts"></script>

<style scoped>
    .listing-detail--masthead {
        height: 300px;
        background-color: #fafafa;
        margin-bottom: 120px;
    }
</style>
