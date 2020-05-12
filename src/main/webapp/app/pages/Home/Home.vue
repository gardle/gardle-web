<template>
    <v-container fluid class="pa-0 ma-0 fill-height">
        <v-row class="garden-background relative pa-0 ma-0" justify="center" align="center" style="height: calc(100vh - 64px)">
            <v-col cols="12">
                <v-row align="center" justify="center" class="flex-column">
                    <v-col cols="11" sm="6" xl="4">
                        <v-btn block rounded
                               large elevation="20"
                               color="green"
                               v-text="$t('home.search.explore')"
                               dark @click="handleExploreSearch">
                            Explore all fields
                        </v-btn>
                    </v-col>
                    <v-col cols="5" lg="3">
                        <v-divider dark class="my-8"/>
                    </v-col>
                    <v-col cols="11" sm="6" xl="4">
                        <v-autocomplete
                            :item-text="gardenFieldItem"
                            :items="gardenFieldItems"
                            :loading="isGardenFieldSearchLoading"
                            :label="$t('home.search.orSearch')"
                            :search-input.sync="searchVal"
                            :hide-no-data="searchVal ? searchVal.length <= 3 : true"
                            hide-details
                            return-object
                            item-value="id"
                            background-color="white" class="elevation-20 text-center"
                            no-filter
                            clearable
                            v-model="selectedGardenField"
                            rounded
                            @keyup.enter="handleSearch"
                            append-icon=""
                            solo
                        >
                            <template v-slot:item="{ item }">
                                <v-list-item-content>
                                    <v-list-item-title>{{gardenFieldItem(item)}}</v-list-item-title>
                                </v-list-item-content>
                            </template>
                            <template v-slot:no-data="">
                                <v-list-item>
                                    <v-list-item-content>
                                        <v-list-item-subtitle v-html="$t('home.search.noData')"></v-list-item-subtitle>
                                    </v-list-item-content>
                                </v-list-item>
                            </template>
                        </v-autocomplete>
                    </v-col>
                </v-row>
            </v-col>
            <v-col cols="12" class="absolute" style="bottom:0; left:0; width: 100%">
                <v-row align="center" justify="center" class="flex-column py-2" @click="scrollToPage(1)">
                    <p class="white--text mb-7" v-html="$t('home.howTo')">Learn how it works</p>
                    <v-btn icon large class="white--text jumping">
                        <v-icon>
                            mdi-chevron-triple-down
                        </v-icon>
                    </v-btn>
                </v-row>
            </v-col>
        </v-row>
        <v-row class="garden-background relative pa-0 ma-0" align="center" style="min-height: calc(100vh - 64px)">
            <v-col cols="12" class="pa-0" self-align="center">
                <div>
                    <h2 class="display-3 opaque-overlay-transp">
                        <span v-html="$t('home.description.takeSentence')" class="font-weight-bold outline-text"></span>
                        <span v-html="$t('home.description.nature')" class="font-weight-bold"></span>
                    </h2>
                    <div class="opaque-overlay">
                        <div class="square"></div>
                        <div>
                            <p v-html="$t('home.description.lessee0')" class="mb-0"></p>
                            <ol>
                                <li v-html="$t('home.description.lessee1')"></li>
                                <li v-html="$t('home.description.lessee2')"></li>
                                <li v-html="$t('home.description.lessee3')"></li>
                                <li v-html="$t('home.description.lessee4')"></li>
                            </ol>
                        </div>
                    </div>
                    <h2 class="display-3 opaque-overlay-transp" style="text-align: right">
                        <span class="font-weight-bold outline-text" v-text="$t('home.description.share.or')"> ...or share </span>
                        <span class="font-weight-bold" v-text="$t('home.description.share.it')"> it </span>
                        <span class="font-weight-bold outline-text" v-text="$t('home.description.share.withOthers')"> with others </span>
                    </h2>
                    <div class="opaque-overlay" style="text-align: right; padding-bottom: 25px; justify-content:flex-end;">
                        <div>
                            <p v-html="$t('home.description.renter0')" class="mb-0">
                            </p>
                            <ol>
                                <li v-html="$t('home.description.renter1')"></li>
                                <li v-html="$t('home.description.renter2')"></li>
                                <li v-html="$t('home.description.renter3')"></li>
                                <li v-html="$t('home.description.renter4')"></li>
                            </ol>
                        </div>
                        <div class="square"></div>
                    </div>
                </div>
            </v-col>
        </v-row>
        <v-row class="relative pa-0 ma-0" align="center">
            <v-col cols="12" class="pa-0" self-align="center">
                <div class="amber lighten-2 d-block amber--text text--darken-3">
                    <v-card-actions justify="center" class="pa-4">
                        <span>
                            <strong>DISCLAIMER:</strong>
                            Dieses Projekt ist ein Universitäts Projekt, welches im Rahmen von Advanced Software Engineering an der TU Wien erstellt wurde. <br>
                            Bitte verwenden Sie dieses Projekt nicht mit sensiblen Daten!
                        </span>
                    </v-card-actions>
                </div>
            </v-col>
        </v-row>
    </v-container>
</template>

<script lang="ts" src="./Home.component.ts">
</script>

<style lang="scss" scoped>
    .garden-background{
        background-size: cover;
        background-position: center;
        background-attachment: fixed;
    }

    .opaque-overlay-transp{
        background-color: #fff;
        backdrop-filter: blur(10px);
        color: #000;
        mix-blend-mode: screen;
        opacity: 0.96;
        padding: 25px 50px;
    }

    .square{
        width: 1px;
        background-color: #4e7d15;
        margin: 0 25px;
        flex-shrink: 0;
    }

    .opaque-overlay{
        background-color: #fff;
        display: flex;
        backdrop-filter: blur(10px);
        opacity: 0.96;
        font-family: 'Roboto', sans-serif;
        color: #4e7d15;
        line-height: 2.5;
        padding: 0 25px;
    }


    .opaque-overlay ol{
        list-style-type:number;
        font-size: 1em;
        list-style-position: inside;
        padding-right: 0;
        padding-left: 0;
    }

    .outline-text{
        color: #fff;
        -webkit-text-stroke-width: 1px;
        -webkit-text-stroke-color: #000;
        font-family: 'Roboto', sans-serif;
    }

    .jumping {
        animation: MoveUpDown 1.5s ease-in-out infinite;
        position: absolute;
        bottom: 0;
    }

    @keyframes MoveUpDown {
        0%, 100% {
            bottom: 0px;
        }
        50% {
            bottom: 10px;
        }
    }
</style>
