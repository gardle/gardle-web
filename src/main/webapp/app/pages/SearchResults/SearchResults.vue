<template>
    <v-row no-gutters>
        <portal to="navigationDrawerContent">
            <filter-bar v-on:update-filters="handleFilterUpdate"
                        v-on:reset-filters="handleResetFilters"
                        ref="filter-bar-component"></filter-bar>
        </portal>
        <v-col cols="12">
            <v-container fluid>
                <v-row align="start" justify="center" no-gutters v-if="this.gardenFieldPage.content &&
                                                                      this.gardenFieldPage.pageable">
                    <v-col cols="12" md="10" lg="8" class="pa-4" v-if="hasResults">
                        <template v-for="n in this.gardenFieldPage.content">
                            <v-card :key="n.id" class="my-4 pr-3" outlined @click="showListingDetail(n.id)">
                                <v-row align="center" no-gutters>
                                    <v-col cols="12" sm="3" class="pa-2">
                                        <v-img
                                            :src=n.imageUrl
                                            :height="130"
                                            :width="130"
                                            aspect-ratio="1"
                                            class="grey lighten-2"
                                        >
                                            <template v-slot:placeholder>
                                                <v-row
                                                    class="fill-height ma-0"
                                                    align="center"
                                                    justify="center"
                                                >
                                                    <img height="80%" src="content/images/logo.png" width="80%"/>
                                                </v-row>
                                            </template>
                                        </v-img>
                                    </v-col>
                                    <v-col cols="12" sm="9">
                                        <v-card-title class="pb-1">
                                            <div>
                                                <h3 class="title font-weight-regular">
                                                    {{n.name}}
                                                </h3>
                                                <span class="grey--text caption">
                                                    <span v-html="$t('searchResult.gardenFieldList.item.pricePerMonth',
                                                    [$options.filters.formatNumber(n.pricePerMonth)])"></span>
                                                    &nbsp;
                                                    &bull;
                                                    &nbsp;
                                                    <span v-html="$t('searchResult.gardenFieldList.item.size', [n.sizeInM2])"></span>
                                                </span>
                                            </div>
                                        </v-card-title>
                                        <v-card-text class="subtitle-2 font-weight-regular grey--text text--darken-2">
                                            {{n.description.substring(0,180)}}
                                        </v-card-text>
                                    </v-col>
                                </v-row>
                            </v-card>
                        </template>

                        <v-pagination
                            :length="gardenFieldPage.totalPages"
                            @input="handleUpdatePageNumber"
                            v-model="gardenFieldPage.pageable.pageNumber + 1"
                            :total-visible="$vuetify.breakpoint.xsOnly ? '5' : $vuetify.breakpoint.smOnly ? '10' : '20'"
                        ></v-pagination>
                    </v-col>
                    <v-col cols="12" md="10" lg="8" class="pa-4" v-else>
                        <v-sheet class="text-center">
                            <v-icon size="128" class="mb-4 grey--text text--lighten-2">
                                mdi-flower
                            </v-icon>
                            <p class="grey--text" v-text="$t('searchResult.noResultsText')"></p>
                        </v-sheet>
                    </v-col>
                </v-row>
            </v-container>
        </v-col>
    </v-row>
</template>

<script lang="ts" src="./SearchResults.component.ts">
</script>


<style lang="scss">
    .search-masthead {
        height: 100px;
    }
</style>
