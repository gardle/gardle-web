<template>
    <v-row justify="center">
        <v-col class="py-0 grey lighten-4" cols="12">
            <v-row justify="center" no-gutters>
                <v-col cols="11" sm="9" xl="7">
                    <v-toolbar class="transparent" elevation="0">
                        <v-btn @click="$router.back()" icon>
                            <v-icon>
                                mdi-arrow-left
                            </v-icon>
                        </v-btn>
                        <v-toolbar-title v-text="(isEditing) ? $t('listing.update.title') : $t('listing.create.title')">
                            Create Listing
                        </v-toolbar-title>
                    </v-toolbar>
                </v-col>
            </v-row>
        </v-col>
        <v-col class="mt-8" cols="11" sm="9" xl="7">
            <v-text-field :error-messages="nameErrors"
                          :label="$t('listing.common.form.label.name')"
                          @blur="$v.listing.name.$touch()"
                          @input="$v.listing.name.$touch()"
                          outlined
                          type="text"
                          v-model="listing.name"/>

            <v-textarea :counter="description_length"
                        :label="$t('listing.common.form.label.description')"
                        auto-grow
                        outlined
                        type="text"
                        v-model="listing.description"/>

            <v-row dense justify="center">
                <v-col cols="12" sm="6">
                    <v-text-field :error-messages="sizeInM2Errors"
                                  :label="$t('listing.common.form.label.sizeSqm')"
                                  @blur="$v.listing.sizeInM2.$touch()"
                                  @input="$v.listing.sizeInM2.$touch()"
                                  min="0"
                                  outlined
                                  type="number"
                                  v-model="listing.sizeInM2">
                        <template v-slot:append="">
                            m&sup2;
                        </template>
                    </v-text-field>
                </v-col>
                <v-col cols="12" sm="6">
                    <v-text-field :error-messages="pricePerM2Errors"
                                  :hint="(listingPrice) ? $t('listing.common.form.hint.pricePerMonth', [listingPrice]) : ''"
                                  :label="$t('listing.common.form.label.priceSqm')"
                                  @blur="$v.listing.pricePerM2.$touch()"
                                  @input="$v.listing.pricePerM2.$touch()"
                                  min="0"
                                  outlined
                                  type="number"
                                  persistent-hint
                                  v-model="listing.pricePerM2">
                        <template v-slot:append="">
                            €/m&sup2;
                        </template>
                    </v-text-field>
                </v-col>
                <v-col cols="12">
                    <v-autocomplete :error-messages="listingAddressErrors"
                                    :item-text="addressItem"
                                    :items="addresses"
                                    :label="$t('listing.common.form.label.address')"
                                    :loading="isLocationSearchLoading"
                                    :search-input.sync="locationSearchString"
                                    @blur="$v.listingAddress.$touch()"
                                    @input="$v.listingAddress.$touch()"
                                    clearable
                                    hide-no-data
                                    item-value="osm_id"
                                    no-filter
                                    outlined
                                    return-object
                                    v-model="listingAddress">
                        <template v-slot:item="{ item }">
                            <v-list-item-content>
                                <v-list-item-title>{{addressItem(item)}}</v-list-item-title>
                            </v-list-item-content>
                        </template>
                    </v-autocomplete>
                </v-col>
            </v-row>

            <v-divider/>
            <v-subheader v-text="$t('listing.common.form.heading.features')">Features</v-subheader>

            <v-row dense>
                <v-col cols="11" sm="6">
                    <v-checkbox :label="$t('listing.common.form.label.roofed')"
                                v-model="listing.roofed"/>

                    <v-checkbox :label="$t('listing.common.form.label.glassHouse')"
                                v-model="listing.glassHouse"/>

                    <v-checkbox :label="$t('listing.common.form.label.high')"
                                v-model="listing.high"/>
                </v-col>
                <v-col cols="11" sm="6">
                    <v-checkbox :label="$t('listing.common.form.label.water')"
                                v-model="listing.water"/>

                    <v-checkbox :label="$t('listing.common.form.label.electricity')"
                                v-model="listing.electricity"/>

                    <v-text-field :error-messages="phValueErrors"
                                  :label="$t('listing.common.form.label.phValue')"
                                  @blur="$v.listing.phValue.$touch()"
                                  @input="$v.listing.phValue.$touch()"
                                  max="14"
                                  min="0"
                                  outlined
                                  type="number"
                                  v-model="listing.phValue">
                    </v-text-field>
                </v-col>
            </v-row>

            <v-divider/>
            <v-subheader v-text="$t('listing.common.form.heading.images')">Images</v-subheader>

            <image-upload
                    :existing-images="(isEditing) ? existingImages : []"
                    @added-image="handleAddImage"
                    @delete-existing-image="handleDeleteExistingImage"
                    @removed-image="handleRemoveImage"></image-upload>

            <v-row :class="['mt-10', ($vuetify.breakpoint.xsOnly) ? 'px-4' : '']">
                <v-spacer/>
                <v-btn :class="['grey--text text--lighten-1',
              ($vuetify.breakpoint.xsOnly) ? 'mb-3' : '']" text :block="$vuetify.breakpoint.xsOnly">
                    {{$t('listing.common.form.button.reset')}}
                </v-btn>
                <v-btn :block="$vuetify.breakpoint.xsOnly" :disabled="$v.$invalid" @click="submit"
                       class="green white--text"
                       depressed>
                    {{(isEditing)
                    ? $t('listing.update.form.button.update')
                    : $t('listing.create.form.button.submit')}}
                </v-btn>
            </v-row>

        </v-col>
    </v-row>
</template>

<script lang="ts" src="./CreateListing.component.ts"></script>

<style scoped>

</style>
