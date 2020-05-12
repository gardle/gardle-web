<template>
    <div>
        <v-chip-group column @change="updateLeasingStatus">
            <v-chip v-for="(item, i) in transitions" :key="i" label small
                    :disabled="!availableTransitions.includes(item.id)"
                    :value="item.type"
                    :class="[(item.id === currentStatus.id) ? `${item.color}` : '']">
                {{$t(`leasing.status.${item.type}`)}}
            </v-chip>
        </v-chip-group>
    </div>
</template>

<script lang="ts" src="./LeasingStatusSwitcher.component.ts"></script>

<style scoped>

</style>
