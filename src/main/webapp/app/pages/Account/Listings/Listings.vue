<template>
    <v-row justify="center">
        <v-col cols="12" class="py-0 grey lighten-4">
            <v-row justify="center" no-gutters>
                <v-col cols="11" md="9" xl="7">
                    <v-toolbar elevation="0" class="transparent">
                        <v-toolbar-title>{{$t('listing.title')}}</v-toolbar-title>
                        <v-spacer/>

                        <v-btn v-if="paymentAccountVerified === true" icon @click="handleCreateListingClick">
                            <v-icon>
                                mdi-plus
                            </v-icon>
                        </v-btn>
                    </v-toolbar>
                </v-col>
            </v-row>
        </v-col>
        <v-col cols="11" md="9" xl="7">
            <listing-list v-if="paymentAccountVerified === true" :listings="listings"
                          @create-listing="handleCreateListingClick"
                          @delete-listing="handleDeleteListing"
                          @edit-listing="handleEditListing"
                          @details-listing="handleDetailsListing"></listing-list>
            <v-sheet v-else-if="paymentAccountVerified === false" class="info lighten-2 info--text text--darken-4 pa-5">
                <div class="py-4 d-flex align-center justify-start">
                    <v-icon size="24" class="pr-3 info--text text--darken-4">
                        mdi-check
                    </v-icon>
                    <h3 class="headline" v-html="$t('listing.common.messages.verificationNeeded.title')"></h3>
                </div>
                <p class="caption" v-html="$t('listing.common.messages.verificationNeeded.description')"></p>
                <div class="d-flex">
                    <v-spacer/>
                    <v-btn class="info--text text--darken-4" text v-html="$t('listing.common.messages.verificationNeeded.action')" @click="handleGetAccountLinks">
                    </v-btn>
                </div>
            </v-sheet>
        </v-col>
    </v-row>
</template>

<script lang="ts" src="./Listings.component.ts"></script>

<style scoped>

</style>
