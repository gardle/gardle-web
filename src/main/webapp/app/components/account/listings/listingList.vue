<template>
    <div>
        <template v-if="listings && listings.length > 0">
            <listing-item :key="i" :listing="listing"
                          @delete-listing="$emit('delete-listing', $event)"
                          @details-listing="$emit('details-listing', $event)"
                          @edit-listing="$emit('edit-listing', $event)"
                          v-for="(listing, i) in listings"></listing-item>
        </template>
        <v-sheet class="text-center" v-else-if="listings.length === 0">
            <v-icon class="mb-4 grey--text text--lighten-2" size="128">
                mdi-flower
            </v-icon>
            <p class="grey--text">You currently don't have any gardenfields. <br> But don't worry, to get started just
                press
                <v-btn @click="$emit('create-listing')" class="d-inline" icon small>
                    <v-icon>
                        mdi-plus
                    </v-icon>
                </v-btn>
            </p>
        </v-sheet>
    </div>
</template>

<script lang="ts" src="./listingList.component.ts"></script>

<style scoped>

</style>
